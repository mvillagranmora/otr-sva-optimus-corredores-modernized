angular.module('ModuloPlataforma').controller('TestInputDateCtrl',function($scope,$translate,$http){
			
	
			$scope.get = $http.get;
			
			$scope.jqueryajax = function (url) {
					$.ajax({
					        url:''+url,
					        success: function (response) {
					            //console.log("jqueryajax",response);
					        },
					        error: function (response) {
					        	//console.log("jqueryajax",response);
					        },
					    });
					return false;				
			};
			
			$scope.monthSelectorOptions = {
              
            };
			
            $scope.comboOptions2 = {};
            
            
            $scope.comboOptions =
            {
              placeholder : 'hola',
              dataTextField: "text",
              dataValueField: "value",
              dataSource: [
                  { text: "Cotton", value: "1" },
                  { text: "Polyester", value: "2" },
                  { text: "Cotton/Polyester", value: "3" },
                  { text: "Rib Knit", value: "4" }
              ],
            };

            $scope.options = {
                min : "10:00 AM",
            };
            $scope.optionsMon =
            {
              decimals : 6,
            };
            $scope.optionsPorc =
            {
              min: 1,
              step : 0.2,
              decimals: 2,
              max:10,
              placeholder:"Ingrese Tasa"
            };
            $scope.optionsNum =
            {
            	format : "c3"    ,
            		peco1: "jorge le gusta",
            };
            
            $scope.optionsNum2 =
            {
              format : "c5",
              peco2: "raul le gusta",
            	  
            };            

            $scope.mainGridOptions = {
                dataSource: {
                    type: "odata",
                    transport: {
                        read: "http://demos.telerik.com/kendo-ui/service/Northwind.svc/Employees"
                    },
                    pageSize: 5,
                    serverPaging: true,
                    serverSorting: true
                },
                sortable: true,
                pageable: true,
                filterable: true,
                groupable: true,
                columns: [{
                    field: "FirstName",
                    title: $translate.instant('FIRST_NAME'),
                    width: "120px"
                    },{
                    field: "LastName",
                    title: "Last Name",
                    width: "120px"
                    },{
                    field: "Country",
                    width: "120px"
                    },{
                    field: "City",
                    title: $translate.instant('TRANSACCION'),
                    width: "120px"
                    },{
                    field: "Title"
                }]
            };
            $scope.refreshGrid = function()
            {
              //encontrar el indice de la columna estado
              var t = $('#grid').find('th:contains("Country")').index();
              if (t!=-1)
              {
                $('#grid').find('tr').each(function (i,el)
                {
                  $(this).find('td').eq(t).each(function (i,el){
                    if ($(el).text() == $scope.pais) $(el).parent().css('background-color','blue');
                    else $(el).parent().css('background-color','red');
                  });
                });
              };
            };

});
