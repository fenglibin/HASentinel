var app = angular.module('sentinelDashboardApp');

app.controller('AuthorityRuleControllerV2', ['$scope', '$stateParams', 'AuthorityRuleServiceV2', 'ngDialog',
    function ($scope, $stateParams, AuthorityRuleServiceV2, ngDialog) {
        $scope.app = $stateParams.app;

        $scope.rulesPageConfig = {
            pageSize: 10,
            currentPageIndex: 1,
            totalPage: 1,
            totalCount: 0
        };

        function getAppRules() {
            AuthorityRuleServiceV2.queryAppRules($scope.app)
                .success(function (data) {
                    if (data.code === 0 && data.data) {
                        $scope.loadError = undefined;
                        $scope.rules = data.data;
                        $scope.rulesPageConfig.totalCount = $scope.rules.length;
                    } else {
                        $scope.rules = [];
                        $scope.rulesPageConfig.totalCount = 0;
                        $scope.loadError = {message: data.msg};
                    }
                })
                .error(function (data) {
                    $scope.loadError = {message: "未知错误"};
                })
        }
        $scope.getAppRules = getAppRules;
        getAppRules();

        var authorityRuleDialog;

        $scope.editRule = function (rule) {
            $scope.currentRule = angular.copy(rule);
            $scope.authorityRuleDialog = {
                title: '编辑授权规则',
                type: 'edit',
                confirmBtnText: '保存'
            };
            authorityRuleDialog = ngDialog.open({
                template: '/app/views/dialog/authority-rule-dialog.html',
                width: 680,
                overlay: true,
                scope: $scope
            });
        };

        $scope.addNewRule = function () {
            $scope.currentRule = {
                app: $scope.app,
                rule: {
                    strategy: 0,
                    limitApp: ''
                }
            };
            $scope.authorityRuleDialog = {
                title: '新增授权规则',
                type: 'add',
                confirmBtnText: '新增',
                showAdvanceButton: true
            };
            authorityRuleDialog = ngDialog.open({
                template: '/app/views/dialog/authority-rule-dialog.html',
                width: 680,
                overlay: true,
                scope: $scope
            });
        };

        $scope.saveRule = function () {
            if (!AuthorityRuleServiceV2.checkRuleValid($scope.currentRule.rule)) {
                return;
            }
            if ($scope.authorityRuleDialog.type === 'add') {
                addNewRuleAndPush($scope.currentRule);
            } else if ($scope.authorityRuleDialog.type === 'edit') {
                saveRuleAndPush($scope.currentRule, true);
            }
        };

        function addNewRuleAndPush(rule) {
            AuthorityRuleServiceV2.addNewRule(rule).success(function (data) {
                if (data.success) {
                    getAppRules();
                    authorityRuleDialog.close();
                } else {
                    alert('添加规则失败：' + data.msg);
                }
            }).error(function (data) {
                if (data) {
                    alert('添加规则失败：' + data.msg);
                } else {
                    alert("添加规则失败：未知错误");
                }
            })
        }

        function saveRuleAndPush(rule, edit) {
            AuthorityRuleServiceV2.saveRule(rule).success(function (data) {
                if (data.success) {
                    alert("修改规则成功");
                    getAppRules();
                    if (edit) {
                        authorityRuleDialog.close();
                    } else {
                        confirmDialog.close();
                    }
                } else {
                    alert('修改规则失败：' + data.msg);
                }
            }).error(function (data) {
                if (data) {
                    alert('修改规则失败：' + data.msg);
                } else {
                    alert("修改规则失败：未知错误");
                }
            })
        }

        function deleteRuleAndPush(entity) {
            if (entity.id === undefined || isNaN(entity.id)) {
                alert('规则 ID 不合法！');
                return;
            }

            AuthorityRuleServiceV2.deleteRule(entity)
                .success(function (data) {
                    if (data.code === 0) {
                        getAppRules();
                        confirmDialog.close();
                    } else {
                        alert('删除规则失败：' + data.msg);
                    }
                })
                .error(function (data) {
                    if (data) {
                        alert('删除规则失败：' + data.msg);
                    } else {
                        alert("删除规则失败：未知错误");
                    }
                })
        }

        var confirmDialog;
        $scope.deleteRule = function (ruleEntity) {
            $scope.currentRule = ruleEntity;
            $scope.confirmDialog = {
                title: '删除授权规则',
                type: 'delete_rule',
                attentionTitle: '请确认是否删除如下授权限流规则',
                attention: '资源名: ' + ruleEntity.rule.resource + ', 流控应用: ' + ruleEntity.rule.limitApp +
                    ', 类型: ' + (ruleEntity.rule.strategy === 0 ? '白名单' : '黑名单'),
                confirmBtnText: '删除'
            };
            confirmDialog = ngDialog.open({
                template: '/app/views/dialog/confirm-dialog.html',
                scope: $scope,
                overlay: true
            });
        };

        $scope.confirm = function () {
            if ($scope.confirmDialog.type === 'delete_rule') {
                deleteRuleAndPush($scope.currentRule);
            } else {
                console.error('error');
            }
        };
    }]);