'use strict';
angular.module('docs')
.controller('RegisterRequests', function($scope, Restangular, $dialog, $translate) {
  $scope.loadRequests = function() {
    Restangular.one('user').getList('register_requests').then(function(requests) {
      $scope.requests = requests;
    }, function(error) {
      console.log('Error loading requests:', error);
      var title = $translate.instant('Error');
      var msg = $translate.instant('Failed_to_load_requests');
      var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
      $dialog.messageBox(title, msg, btns);
    });
  };

  $scope.approveRequest = function(request) {
    // 调用后端批准接口，例如发送 POST 请求到 /user/approve_register_request
    Restangular.one('user').post('approve_register_request', { username: request.username })
      .then(function(response) {
        // 批准成功后，重新加载列表或者提示成功
        $scope.loadRequests();
      }, function(error) {
        console.log('Error approving request:', error);
        // 显示错误提示
      });
  };

  $scope.deleteRequest = function(request) {
    // 如果需要，也可以提供删除功能
    Restangular.one('user').customDELETE('register_request', { username: request.username })
      .then(function() {
        $scope.loadRequests();
      }, function(error) {
        console.log('Error deleting request:', error);
      });
  };

  // 初始化加载
  $scope.loadRequests();
});