angular.module('ModuloPlataforma').directive('bcsInputDocumentoIdentidad', function($parse) {
	return {
		restrict: 'E',
	    require: 'ngModel',
		replace: false,
		scope: {
			//configuracion documento identidad
			ngModel 		: '=?',//retorna el documento de identidad principal
			documentoAsociado	: '=?', //retorna el documento de identidad asociado ej: IUP=2012345678, docAso=12345678
			codigoDocumentoAsociado	:'=?', //retorna el codigo del documento asociado ej: IUP=2012345678, codDocAso=20
			cambiarTipoDocumentoIdentidad : '=?',
			cambiarDocumentoIdentidad	: '=?',//para setear el documento identidad
			placeholder		: '=?',
			focus			: '@?',
			blur			: '&?',
			requerido		: "=?", //true or false

			//configuracion tipo documento identidad
			idTipoPersona	: '=?', //N, J, M			
			idDefecto		: '=?', //setear por defecto el tipo documento identidad
			forzarTipoDocumento	: '=?', // S: utiliza el idDefecto para iniciar el combobox y no permite que se cambie. N: Permite cambio de tipo documentos.
			tipo			: '=?', //configuracion del combobox (servicio, campos, etc)
			idTipoDocumento	: '=?', //IUP,R,P, DNI, PAS, ETC...
			idTipoDocumentoAsociado :'=?',//R,P, DNI, PAS, ETC...
			workingNet		: '=?', 
			
			//para filtrar desde el html
			usaFiltroExterno : '=?',  // true: utiliza fitros enviados desde la UI
			filtroTipoDocumento: '=?',
			//general
			tipoDocumentoIdentidadActivado	: '=?',
			documentoIdentidadActivado	: '=?',

			//solo para peru
			activarDocumentoIdentidadFicticio : '=?',
			activarRutInterno : '=?'
		},
		templateUrl: 'directives/bcs-input-documento-identidad/bcs-input-documento-identidad.html',
		controller : function($scope){
			this.getSeleccionado = function(){
				return $scope.ngModel;
			}
		},

		link: function(scope, element, attrs, controller, transcludeFn) {
			
			scope.largoDocumento = 12;
			if (!scope.placeholder) {
				scope.placeholder = "";
			}
			scope.idTipoDocumentoInterno = scope.idDefecto || "";
			scope.relacionados="";
			scope.forzarTipoDocumento = scope.forzarTipoDocumento || "N";
			scope.tipoDocumentoIdentidadActivado = (!!scope.tipoDocumentoIdentidadActivado);
			scope.documentoIdentidadActivado = (!!scope.documentoIdentidadActivado);
			scope.idTipoDocumentoAsociado = "";
			scope.validaRut = false;
			scope.requerido = scope.requerido || false;
			scope.habilitado = true;
					
			scope.conf={
				documentoIdentidad :{
					//CHILE
					R :{largo:12, codigo:"00", padding:"" , soloNumeros:"N"},
					P:{largo: 10, codigo: "20", padding:"", soloNumeros:"N"},
					X:{largo: 8, codigo: "00", padding:"", soloNumeros:"N"},
					TAX:{largo: 15, codigo: "00", padding:"", soloNumeros:"N"},//Largo SII
					//PERU
					IUP:{largo: 14, codigo:"", padding:"", soloNumeros:"N"},				
					DNI:{largo: 8, codigo:"02", padding:"", soloNumeros:"S"},
					RUC:{largo: 11, codigo:"03", padding:"", soloNumeros:"N"},
					CE:{largo: 8, codigo:"20", padding:"", soloNumeros:"N"},
					PAS:{largo: 11, codigo:"20", padding:"", soloNumeros:"N"},
					CDI:{largo: 8, codigo:"00", padding:"", soloNumeros:"N"},
					FIC:{largo: 8, codigo:"22", padding:"", soloNumeros:"N"},
					PAI:{largo: 11, codigo:"22", padding:"", soloNumeros:"N"},
					RUT:{largo: 8, codigo:"20", padding:"left", soloNumeros:"N"}
				}
			}
			scope.filtroTipoDocumentoIdentidad = {};
			scope.initConfg =function(){
				if(scope.forzarTipoDocumento==="S"){
					//cargar por defecto el rut cavali
					scope.filtroTipoDocumentoIdentidad = {
						PorDefecto : {field: "PorDefecto", operator: "eq", value: "N"},
						TipoPersona : {field: "IdPais", operator: "eq", value: scope.SesionIdPaisSistema}
					}
					scope.tipoDocumentoIdentidadActivado=false;
				}
				scope.largoDocumento = (scope.conf.documentoIdentidad[scope.idTipoDocumentoInterno])?scope.conf.documentoIdentidad[scope.idTipoDocumentoInterno].largo : scope.largoDocumento;
			}
			scope.initConfg();

			scope.$watch("activarRutInterno", function(nuevo, antiguo) {
				if (scope.$root.SesionLocale === "PE") {
					if (nuevo!==undefined) {
						if (nuevo) {
							if (scope.idTipoDocumentoAsociado==="RUT" && (scope.idTipoPersona=="N" || scope.idTipoPersona=="M" || scope.idTipoPersona=="J")) {
								scope.largoDocumento = 8;
							} else {
								scope.largoDocumento = 11;
							}
						} else {
							scope.largoDocumento = 8;
						}
					} else {
						scope.largoDocumento = 8;
					}
				}
			

			});

			scope.$watch("cambiarDocumentoIdentidad", function(nuevo, antiguo){
				scope.documentoAsociado = nuevo  || "";
				//si es ficticio, padding de 0 a la izquierda
				if(scope.idTipoDocumentoAsociado==="FIC" || scope.activarRutInterno){
					scope.documentoAsociado = util.PadLeft(scope.documentoAsociado+"","0",scope.largoDocumento);
				}
			});
			scope.$watch("cambiarTipoDocumentoIdentidad", function(nuevo, antiguo){
				scope.idTipoDocumentoInterno = nuevo;
			});
				
			scope.$watch("idTipoPersona", function(nuevo,antiguo){
				var idPais = {field: "IdPais", operator: "eq", value: scope.$root.SesionIdPaisSistema};
				if(nuevo){
					if (!scope.usaFiltroExterno){
							//CHILE
							var r = { field: "IdTipoDocumentoIdentidad", operator: "eq", value: "R" };
							var p = { field: "IdTipoDocumentoIdentidad", operator: "eq", value: "P" };
							//PERU
							var dni = { field: "IdTipoDocumentoIdentidad", operator: "eq", value: "DNI" };
							var ruc = { field: "IdTipoDocumentoIdentidad", operator: "eq", value: "RUC" };
							var rut = { field: "IdTipoDocumentoIdentidad", operator: "eq", value: "RUT" };
							var ce = { field: "IdTipoDocumentoIdentidad", operator: "eq", value: "CE" };
							var pas = { field: "IdTipoDocumentoIdentidad", operator: "eq", value: "PAS" };
							var cdi = { field: "IdTipoDocumentoIdentidad", operator: "eq", value: "CDI" };
							var fic = { field: "IdTipoDocumentoIdentidad", operator: "eq", value: "FIC" };
							var iup = { field: "IdTipoDocumentoIdentidad", operator: "eq", value: "IUP" };
							var pai = { field: "IdTipoDocumentoIdentidad", operator: "eq", value: "PAI" };
		
							//filter docs by pais
							var filterBy = [];
							var complex = [];								
							if(scope.$root.SesionLocale==="PE"){
		
								scope.conf.documentoIdentidad["FIC"].codigo = (nuevo==="J" || nuevo==="M") ? "22" : "20";
								//scope.conf.documentoIdentidad["FIC"].largo = (nuevo==="J") ? 11 : 8;
								
								if(scope.forzarTipoDocumento!=="S"){
									if(nuevo==="N"){
										filterBy = [dni,ce,pas,fic];
										complex = [idPais, dni,ce,pas,fic];
										if(scope.idTipoDocumentoAsociado==="IUP"){
											filterBy.push(iup);
											complex.push(iup);
										}
										if(scope.idTipoDocumentoAsociado==="RUT"){
											filterBy.push(rut);
											complex.push(rut);
										}
									}else if(nuevo==="M"){
										filterBy = [fic];
										complex = [idPais,fic];
		
									}else if(nuevo==="J"){
										filterBy = [ruc,fic,pai];
										complex = [idPais,ruc,fic,pai];
									}	
								}else{
									filterBy = [idPais];
									complex = [idPais];
								}
							}else{
								//CHILE
								filterBy = [p,r];
								complex = [idPais, r, p];
							}
							scope.filtroTipoDocumentoIdentidad = {
								complejo : complex,
								logic: "and",
								filters: [
									{
									  logic: "or",
									  filters: filterBy
									},
									idPais
								]
							};
							
					}else{//filtroExterno
						
						if(scope.filtroTipoDocumento!==undefined)
						{	
							var nfiltros = new Array();
						
							for (var prop in scope.filtroTipoDocumento) {
								var object = scope.filtroTipoDocumento[prop];
							   	if (object.field !== undefined && object.operator !== undefined && object.value!== undefined && object.value!== "") {
									nfiltros.push({
								        "field"   : object.field,
								        "operator": object.operator,
								        "value"   : object.value
									});
							   	}	
							}
							   	
							
						scope.filtroTipoDocumentoIdentidad = {
								complejo : [idPais],
								logic: "and",
								filters: [
									{
									  logic: "or",
									  filters: nfiltros
									},
									idPais
								]
							};
						};
					};			
				}else{
					scope.filtroTipoDocumentoIdentidad = {
						IdPais : idPais
					}
				}
					
					
			});

			scope.$watch("documentoAsociado", function(nuevo,antiguo){

				nuevo = (!!nuevo) ? nuevo+"" : "";
				scope.documentoAsociado = nuevo;
				if(nuevo!==undefined && nuevo.length <= scope.largoDocumento){
					scope.generarIup(nuevo);	
				}else{
					scope.documentoAsociado = scope.documentoAsociado.substring(0, scope.largoDocumento);
				}
				
			});

			scope.innerBlur = function(){
				if(scope.blur && scope.blur!=undefined){
					scope.blur();	
				}
				//validar si acepta solo numeros
				if(scope.documentoAsociado && scope.documentoAsociado!="" && scope.idTipoDocumentoAsociado && scope.idTipoDocumentoAsociado!=""){
					var conf = scope.conf.documentoIdentidad[scope.idTipoDocumentoAsociado];
					if( conf && conf.soloNumeros && conf.soloNumeros==="S"){
						try{
							if(!$.isNumeric(scope.documentoAsociado)){
								scope.documentoAsociado="";	
							}
						}catch(e){
							scope.documentoAsociado="";
						}
					}
				}
			}

			scope.generarIup = function(documento){
				if(!!scope.idTipoDocumentoInterno){
					if(scope.$root.SesionLocale==="PE" && documento && scope.forzarTipoDocumento!=="S"){
						scope.codigoDocumentoAsociado = scope.conf.documentoIdentidad[scope.idTipoDocumentoInterno].codigo;
						var iup =   scope.codigoDocumentoAsociado + ""+util.PadLeft(documento,"0", scope.largoDocumento)
						iup = util.PadRight(iup,"0",14);
						scope.ngModel = iup;
					}else{
						scope.ngModel = documento;
						if(documento && documento!=="" && scope.conf.documentoIdentidad[scope.idTipoDocumentoInterno].padding!==""){		
							var pad = (scope.conf.documentoIdentidad[scope.idTipoDocumentoInterno].padding==="left")? util.PadLeft : util.PadRight;
							scope.ngModel = pad(documento,"0",scope.largoDocumento);	
						}
					}
				}
			}

			scope.$watch("idTipoDocumentoInterno", function(nuevo,antiguo){
				if(nuevo){
					if(nuevo!=="IUP"){
						if(scope.$root.SesionLocale==="PE" && scope.forzarTipoDocumento!=="S"){
							scope.idTipoDocumento = "IUP";
						}else{
							if(nuevo==="R"){
								scope.validaRut = true;
							}
							scope.idTipoDocumento = nuevo;
						}
					}

					if(nuevo==="FIC" && scope.activarDocumentoIdentidadFicticio!=="S"){
						console.log(scope.deshabilitado);
						//ir a buscar un idSecuencia
						scope.documentoAsociado = "0";
						//scope.deshabilitado = true;
					}else{
						scope.deshabilitado = false;
					}

					scope.idTipoDocumentoAsociado = nuevo;
					scope.largoDocumento = (scope.conf.documentoIdentidad[nuevo])?scope.conf.documentoIdentidad[nuevo].largo : scope.largoDocumento;
					scope.generarIup(scope.documentoAsociado);
				}else{
					scope.ngModel = "";
					scope.idTipoDocumentoAsociado = "";
				}
			});
	
		}
	};
});
