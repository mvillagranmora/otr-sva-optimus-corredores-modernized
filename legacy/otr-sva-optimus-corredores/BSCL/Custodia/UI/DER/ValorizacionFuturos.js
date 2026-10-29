angular.module('ModuloCustodia').controller('CarteraValorizacionFuturosCtrl',function($scope,bcsComunes,$timeout,$compile){

    var filters = new Array();
    
    $scope.oClientePortafolio ={
        tipo : "combobox-cliente-portafolio",
        configuracion :{},
        idActual : 0,
        seleccionado : false,
        filtros: {
            IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
            EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
        },
    };
    $scope.Buscar = function(){

        filters = [];

        if($scope.oClientePortafolio.seleccionado && $scope.oClientePortafolio.seleccionado!== null){
            filters.push({"field": "IdPortafolio","operator": "eq", "value"   :  $scope.oClientePortafolio.seleccionado })
        }
        
        dataSourceValorizacionFuturos.filter(filters);
    };
    $scope.Limpiar = function(){

    };

    var getColumns = function(){
		return [
			{field: 'NombreCliente',      title: 'UI_NOMBRE_CLIENTE',      width: '70px', groupable: true},
			{field: 'Instrumento',        title: 'UI_INSTRUMENTO',         width: '70px', groupable: true},
			{field: 'FechaVencimiento',   title: 'UI_FECHA_VENCIMIENTO',   width: '70px', groupable: true},
			{field: 'Cantidad',           title: 'UI_CANTIDAD',            width: '70px',template : " <label> #:  (util.bcs_formato(Cantidad,'GEN','Cantidad','CLP', '','')).valor #</label>"},
            {field: 'PrecioAnterior',     title: 'UI_PRECIO_ANTERIOR',     width: '70px',template : " <label> #:  (util.bcs_formato(PrecioAnterior,'GEN','Precio','CLP', '','')).valor #</label>"},
            {field: 'PrecioBase',         title: 'UI_PRECIO_BASE',         width: '70px',template : " <label> #:  (util.bcs_formato(PrecioBase,'GEN','Precio','CLP', '','')).valor #</label>"},
            {field: 'ResultadoDiario',    title: 'UI_RESULTADO_DIARIO',    width: '70px',template : " <label> #:  (util.bcs_formato(ResultadoDiario,'GEN','Monto','CLP', '','')).valor #</label>"},
            {field: 'ResultadoAcumulado', title: 'UI_RESULTADO_ACUMULADO', width: '70px',template : " <label> #:  (util.bcs_formato(ResultadoAcumulado,'GEN','Monto','CLP', '','')).valor #</label>"}
            ]
        };

    var getSchema = function(){
        return {
            model: {
                fields: {
                    NombreCliente       : {type: 'string'},
                    Instrumento         : {type: 'string'},
                    FechaVencimiento    : {type: 'string'},
                    Cantidad            : {type: 'number'},
                    PrecioAnterior      : {type: 'number'},
                    PrecioBase          : {type: 'number'},
                    ResultadoDiario     : {type: 'number'},
                    ResultadoAcumulado  : {type: 'number'},
                    
                }
            }
        }
    }

    var dataSourceValorizacionFuturos =  new bcs.DataSource({ 
        type : "odata",
        definition : {
            serverFiltering : true, 
            allowCopy		: { delimeter: "\t" },
            userSettings 	: true,
            filter			: filters,
            sort: { field: "NombreCliente", dir: "desc" },
            pageSize : 200,
            transport: {
                read: {
                    url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=700174",
                    dataType: "json"
                }
            },
            schema : getSchema(),
        },
        
    });

    $scope.confValorizacionFuturos = {
		dataSource: dataSourceValorizacionFuturos,
		columns: getColumns(),	
		pageable: true,
        sortable: true,
        autoBind: false,		
		groupable: true,
		filterable: true,
		height: 600,
		excel: {allPages: true,},
        selectable: "row"
	};

	

});