angular.module('ModuloPlataforma').controller('gFormularioCtrl',function($scope,$translate,bcsAlerta,OPCIONES_BCSALERTA,bcsComunes,$timeout,$rootScope,$translate,$http){	
	/*********************************************
	 *  FUNCIONES
	 * *******************************************/
	$scope.verDetalle = function(item) {
		var id1=bcsComunes.ventanaAbrir("gFormularioDetalle",{ IdDetalle: item },false,$scope.verDetalleModal,$scope.winID);
		var promesa=bcsComunes.ventanaPromesa(id1);					

		
   		$.when(promesa).then(function(data){
   			bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_DETALLE") + ' :'+ data);
      	});
	};	
	$scope.verAlerta = function() {
		var t = bcsAlerta.crear("PREGUNTA","Cerrar SAP Business One detendrá todos los procesos en ejecución y cerrará todas las ventanas abiertas. ¿Desea continuar?");
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR) bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_DETALLE") + ' : ACEPTAR');
			else bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_DETALLE") + ' : CANCELAR');
		});
		return;
	};
	$scope.enviarNotificacion = function() {
		$scope.msgcounter++;		
		if ($scope.msgcounter%3==0) bcsComunes.msg("info","info","Este es un mensaje de notificación tipo info "+$scope.msgcounter);
		if ($scope.msgcounter%3==1) bcsComunes.msg("error","error","Este es un mensaje de notificación tipo error"+$scope.msgcounter);
		if ($scope.msgcounter%3==2) bcsComunes.msg("warning","warning","Este es un mensaje de notificación tipo warning "+$scope.msgcounter);
	};
	$scope.comprar = function() {
		bcsComunes.msg("info","info","Clic en el boton comprar");
		bcsComunes.ventanaOverlay($scope.winID,true);
		$timeout(function() { 
			bcsComunes.ventanaOverlay($scope.winID,false);
			$scope.btncomprarhabilitado=true; 
		},3000);
	};	
	$scope.limpiar = function() {
		$scope.btncomprarhabilitado=true;
		$scope.data.simbolo2="EUR";		
		bcsComunes.msg("info","info","Clic en el boton limpiar");
		$scope.foco=true;		
	};	

    $scope.dataLarge="";
    for (var i=1;i<5000;i++) $scope.dataLarge+=" DATA ";
    
	/*********************************************
	 *  VARIABLES
	 * *******************************************/
    $scope.winID=bcsComunes.ventanaWinId($scope);
    bcsComunes.ventanaAutoOverlay($scope.winID,"#UI_CIUDAD input",3000);
    
	$scope.msgcounter=1;
	$scope.verDetalleModal=false;
	$scope.IdDetalle = 12345678;
	$scope.btncomprarhabilitado=true;
	$scope.foco=false;
	
	$scope.data = {};
	$scope.data.kngMonto=1234.123456789;
	$scope.data.simbolo="UF";
	$scope.data.decimales=3;
	$scope.data.simbolo2="USD";
	$scope.data.decimales2=2;	
	$scope.data.texto="hola Mundo pew";
	$scope.data.kngMonto2=1234.123456789;
	$scope.socio_texto="12.043.628-7";
	$scope.portIDPosfiltro=100592;
	
	$scope.data.CodigoArchivo = "TEST_ARCHIVO_SEPARADOR";
	
	$scope.socio={
		tipo : "combobox-socio-negocio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdEstado : { "field"   : "IdEmpresa","operator": "eq", "value"   :  1 }
		},
	};

	$scope.por={
		tipo : "combobox-portafolio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  0 }
		},
	};

	$scope.pais={
		tipo : "combobox-demo-region",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		idDefecto : 3
	};

	$scope.ciudadFiltroRegionIDA = {field: "RegionID",operator: "eq",  value: 13};
	$scope.ciudadFiltroRegionIDB = {field: "RegionID",operator: "eq",  value: 12};
	$scope.ciudadFiltroNombre    = {field: "TerritoryDescription",operator: "contains",  value: "N"};

	$scope.ciudad={
		tipo : "combobox-demo-territori",
		configuracion :{
		},
		filtros : {
			  complejo : [ $scope.ciudadFiltroRegionIDA,$scope.ciudadFiltroRegionIDB,$scope.ciudadFiltroNombre]  ,
			  logic: "and",
			  filters: [
			    {
			      logic: "or",
			      filters: [
			        $scope.ciudadFiltroRegionIDA ,
			        $scope.ciudadFiltroRegionIDB
			      ]
			    },
			    $scope.ciudadFiltroNombre
			  ]
		},
		idPostFiltro : "60601",
		idActual : false,
		seleccionado : false,
		idDefecto : false
	};

	 $scope.countryNames = [
              "Albania",
              "Andorra",
              "Armenia",
              "Austria",
              "Azerbaijan",
              "Belarus",
              "Belgium",
              "Bosnia & Herzegovina",
              "Bulgaria",
              "Croatia",
              "Cyprus",
              "Czech Republic",
              "Denmark",
              "Estonia",
              "Finland",
              "France",
              "Georgia",
              "Germany",
              "Greece",
              "Hungary",
              "Iceland",
              "Ireland",
              "Italy",
              "Kosovo",
              "Latvia",
              "Liechtenstein",
              "Lithuania",
              "Luxembourg",
              "Macedonia",
              "Malta",
              "Moldova",
              "Monaco",
              "Montenegro",
              "Netherlands",
              "Norway",
              "Poland",
              "Portugal",
              "Romania",
              "Russia",
              "San Marino",
              "Serbia",
              "Slovakia",
              "Slovenia",
              "Spain",
              "Sweden",
              "Switzerland",
              "Turkey",
              "Ukraine",
              "United Kingdom",
              "Vatican City"
                  ];
	 $scope.cargarArchivo = function(){
		 var datosArchivo = {
				 codigoArchivo: $scope.data.CodigoArchivo,
				 titulo: $translate.instant("TEST")//el titulo es opcional, en caso de enviarlo se desplegara: Carga Archivo - XXXX; caso contrario: Carga Archivo.
				 };
		 var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo);
			
        var promesa=bcsComunes.ventanaPromesa(id1);                               
        promesa.then(function(data){
        	//el objeto data, contiene la informacion del archivo en formato JSON.
        	console.log(data);
    	}); 
	 }
	 
	  
	 
	 
	 $scope.guardarArchivo = function() {
		 $http.get('/BSCL/Plataforma/GWJAVA/NOR/files/download?schema=BSCL&tipo=reporte&idDocumento=1&idProceso=94433',{}, {responseType:'arraybuffer'})
		  .success(function (response) {
		       var file = new Blob([response]);
		       var fileURL = URL.createObjectURL(file);
		       saveAs(file,"ejemplo.txt");
		});
		 //var dataURI = "data:text/plain;base64," + kendo.util.encodeBase64("EJEMPLO DE GUARDAR");
		 //kendo.saveAs({dataURI: dataURI, fileName: "ejemploJG.txt"});
	 }
	 
	 $scope.validarRangoFecha = function(){
		 
		 var resp = util.validarRangoFecha($scope.fechaDesde, $scope.fechaHasta);
		 if(resp.estado==="NOK"){
			 bcsComunes.msg("warning","INFO", resp.mensaje);
			 return false
		 }
	 	
	 };
	 
});

