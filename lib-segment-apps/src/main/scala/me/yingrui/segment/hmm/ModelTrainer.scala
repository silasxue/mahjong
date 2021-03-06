package me.yingrui.segment.hmm

import java.io.{BufferedReader, File, FileInputStream, InputStreamReader}

import me.yingrui.segment.pinyin.{Pair, Window}

import scala.collection.mutable.{HashMap, Map}

/**
  * Tool to train HMM models
  */
object ModelTrainer extends App {

  private val ngramLength = 2
  private var pii = HashMap[Int,Int]()
  private var emisMatrix = HashMap[Int, Map[Int,Int]]()
  private val model = new HmmModel

//  val filename = "ner-corpus.txt"
  val filename = "training.txt"

  train(filename)
  build()
//  save("ner-hmm.m")
  save("segment-hmm.m")

  def build() {
    getNgram().buildIndex(1)
    model.buildEmission(emisMatrix)
    model.buildPi(pii)

    model.buildViterbi
    clear()
  }

  private def getStateBank() : NodeRepository = {
    return model.getStateBank
  }

  private def getObserveBank() : NodeRepository = {
    return model.getObserveBank
  }

  private def getNgram() : Trie = {
    return model.getNgram
  }

  private def clear() {
    pii.clear()
    emisMatrix.clear()
    pii = null
    emisMatrix = null
  }

  private def save(file: String) {
    model.save(file)
  }

  private def train(filename: String) {
    val file = new File(filename)
    if (file.exists()) {
      println("[ModelBuilder] train file " + file.getAbsolutePath())
      try {
        val br = new BufferedReader(
          new InputStreamReader(
            new FileInputStream(file), "UTF-8"))
        analysisCorpus(br)
        br.close()
      } catch {
        case ex: Throwable =>
          ex.printStackTrace()
      }
    }
  }

  private def analysisCorpus(br: BufferedReader) {
    val window = new Window[Pair[String,String]](ngramLength)
    var line = br.readLine()
    while (null != line) {
      val cur = parseLine(line.trim())

      if (null == cur) {
        window.clear()
      } else {
        window.add(cur)
        var o = cur.getKey()
        val isHeadOfWord = o.startsWith("Head")
        o = if(isHeadOfWord) o.substring(4) else o
        var observe = Node(o)
        observe = getObserveBank().add(observe)
        val s = cur.getValue()
        var state = Node(s)
        state = getStateBank().add(state)

        //Pii
        if (isHeadOfWord)
        {
          val index = state.getIndex()
          val c = if(pii.contains(index)) pii(index) + 1 else 1
          pii += (index -> c)
        }

        statisticNGram(window.toArray())

        //Emission
        val si = state.getIndex()
        val o1 = observe.getIndex()
        var row: Map[Int,Int] = null
        if (emisMatrix.contains(si)) {
          row = emisMatrix(si)
        } else {
          row = HashMap[Int,Int]()
          emisMatrix += (si -> row)
        }
        val count = if(row.contains(o1)) row(o1) + 1 else 1
        row += (o1 -> count)
      }
      line = br.readLine()
    }
  }

  private def parseLine(line: String):Pair[String,String] = {
    var ret: Pair[String, String] = null
    if ("".equals(line) || line.startsWith("c")) {
      return ret
    }

    val pair = line.split("\\s")

    if (null != pair && pair.length == 2) {
      ret = new Pair[String,String](pair(0), pair(1))
    }

    return ret
  }

  private def statisticNGram(array: List[Pair[String,String]]) {
    val ch = new Array[Int](array.length)
    for (i <- 0 until ch.length) {
      ch(i) = getStateBank().get(array(i).getValue()).getIndex()
    }
    getNgram().insert(ch)
  }

}
