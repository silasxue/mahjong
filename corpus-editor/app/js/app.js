'use strict';

/* App Module */

angular.module('corpusEditor', ['corpusEditorFilters', 'corpusEditorServices', 'corpusEditorDirectives']).
    config(['$routeProvider', function ($routeProvider) {
    $routeProvider.
        when('/article', {
            templateUrl: 'layout.html',
            mainTemplateUrl: 'partials/article-list.html',
            sidebarTemplateUrl: 'partials/article-list-side.html',
            controller: ArticleListCtrl}).
        when('/article/:articleId', {
            templateUrl: 'layout.html',
            mainTemplateUrl: 'partials/article-detail.html',
            sidebarTemplateUrl: 'partials/article-detail-side.html',
            controller: ArticleDetailCtrl}).
        when('/dictionary', {
            templateUrl: 'layout.html',
            mainTemplateUrl: 'partials/dictionary/main.html',
            sidebarTemplateUrl: 'partials/sidebar-index.html',
            controller: DictionaryCtrl}).
        otherwise({redirectTo:'/article'});
}]);
