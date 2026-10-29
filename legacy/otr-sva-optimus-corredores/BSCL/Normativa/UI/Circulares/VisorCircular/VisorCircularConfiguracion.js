angular.module('ModuloNormativa').factory('bcsVisorCircularConfig', [ '$resource', 
function($resource) {
	
	var modelo = {};
	
	/**
	 * Schema de la base de datos.
	 */
	var SCHEMA = 'BSCL';
	
	/**
	 * ID de la vista que contiene todos los servicios y vistas.
	 */
	var ID_VISTA_CONSULTA_SERVICIO = 1229;
	
	/**
	 * Id  de la vista que contiene los campos.
	 */
	var ID_VISTA_CONSULTA_CAMPOS = 1473;
	
	/**
	 * Tipos de datos definidos.
	 */
    var TYPE_AMOUNT = 'AMOUNT';
    var TYPE_BOOL = 'BOOL';
    var TYPE_DATE = 'DATE';
    var TYPE_DATE_FILTER = 'DATE_FILTER';
    var TYPE_DECIMAL = 'DECIMAL';
    var TYPE_ID = 'ID';
    var TYPE_ID_CLIENTE = 'ID_CLIENTE';
    var TYPE_ID_PORTAFOLIO = 'ID_PORTAFOLIO';
    var TYPE_NEMO = 'NEMO';
    var TYPE_QTY = 'QTY';
    var TYPE_RUT = 'RUT';
    var TYPE_RUT_CLIENTE = 'RUT_CLIENTE';
    var TYPE_TEXT = 'TEXT';
	
    
    /**
	 * Metodos a incorporar a cada campo para otorgar comportamiento.
	 */
    function FieldMethods() {

        this.isAmount = function() {
            return this.TipoDato === TYPE_AMOUNT;
        }

        this.isDecimal = function() {
            return this.TipoDato === TYPE_DECIMAL;
        }

        this.isText = function() {
            switch (this.TipoDato) {
            case TYPE_NEMO:
            case TYPE_RUT:
            case TYPE_RUT_CLIENTE:
            case TYPE_TEXT:
                return true;
            default:
                return false;
            }
        }

        this.isList = function() {
            return this.TipoDiccionario != null &&
                this.TipoDiccionario.length > 0;
        }

        this.isDate = function() {
            return this.TipoDato === TYPE_DATE
                || this.TipoDato === TYPE_DATE_FILTER;
        }

        this.isNumber = function() {
            switch (this.TipoDato) {
            case TYPE_AMOUNT:
            case TYPE_DECIMAL:
            case TYPE_QTY:
                return true;
            default:
                return false;
            }
        }

        this.getAttributes = function() {
            switch (this.TipoDato) {
            case TYPE_AMOUNT:
            case TYPE_DECIMAL:
            case TYPE_ID:
            case TYPE_ID_CLIENTE:
            case TYPE_ID_PORTAFOLIO:
            case TYPE_QTY:
            case TYPE_RUT:
            case TYPE_RUT_CLIENTE:
                return { style : 'text-align:right;' };
            case TYPE_DATE:
            case TYPE_DATE_FILTER:
                return { style : 'text-align:center;' };
            default:
                return { style : 'text-align:left;' };
            }
        }

        this.getFormat = function() {
            switch (this.TipoDato) {
            case TYPE_AMOUNT:
                return '{0:###,###,###,###,##0}';
            case TYPE_DECIMAL:
                return '{0:n' + this.CantidadDecimales + '}';
            case TYPE_DATE:
            case TYPE_DATE_FILTER:
                return '{0:dd-MM-yyyy}';
            case TYPE_QTY:
                return '{0:###,###,##0}';
            default:
                return null;
            }
        }

        this.getType = function() {
            switch (this.TipoDato) {
            case TYPE_DATE:
            case TYPE_DATE_FILTER:
                return 'date';
            case TYPE_AMOUNT:
            case TYPE_DECIMAL:
            case TYPE_ID:
            case TYPE_ID_CLIENTE:
            case TYPE_ID_PORTAFOLIO:
            case TYPE_QTY:
                return 'number';
            default:
                return 'string';
            }
        }

        this.getWidth = function() {
            switch (this.TipoDato) {
            case TYPE_AMOUNT:
            case TYPE_DECIMAL:
                return '100px';
            case TYPE_DATE:
            case TYPE_DATE_FILTER:
                return '100px';
            case TYPE_ID:
            case TYPE_ID_CLIENTE:
            case TYPE_ID_PORTAFOLIO:
                return '80px';
            case TYPE_NEMO:
                return '100px';
            case TYPE_QTY:
                return '100px';
            case TYPE_RUT:
            case TYPE_RUT_CLIENTE:
                return '80px';
            default:
                return '100px';
            }
        }

        this.isCommon = function() {
            return this.Comun === 'S';
        }

        this.isKey = function() {
            return this.Clave === 'S';
        }

        this.isSumable = function() {
            switch (this.TipoDato) {
            case TYPE_AMOUNT:
            case TYPE_DECIMAL:
            case TYPE_QTY:
                return true;
            default:
                return false;
            }
        }

        this.isFiltrable = function() {
            return this.isNumber() || this.isDate() || this.isList();
        }

        this.isEditable = function() {
            return this.Editable === 'S';
        }

        this.isRequired = function() {
            return this.Obligatorio === 'S';
        }

        this.isHiddenInGrid = function() {
            return this.OcultoEnGrilla === 'S';
        }

        this.isHiddenInForm = function() {
            return this.OcultoEnMantenedor === 'S';
        }

        this.htmlRequired = function() {
            if (this.isEditable() && this.isRequired())
                return 'required';
            return '';
        }

        this.htmlReadonly = function(masivo) {
            if (!this.isEditable() || (masivo && this.ModoMasivo === 'N'))
                return 'readonly';
            return '';
        }

        this.htmlMaxlength = function() {
            if (this.TipoDato === TYPE_AMOUNT)
                return 'maxlength="15"';
            if (this.TipoDato === TYPE_DECIMAL)
                return 'maxlength="22"';
            if (this.TipoDato === TYPE_RUT)
                return 'maxlength="11"';
            if (this.TipoDato === TYPE_RUT_CLIENTE)
                return 'maxlength="11"';
            if (this.TipoDato === TYPE_QTY)
                return 'maxlength="9"';
            if (this.Largo > 0) {
                return 'maxlength="' + this.Largo + '"';
            }
            return '';
        }
    }
	
	var _campos =  null;
	var _columns = [];
	var _fields = {};
	
	var _IdServicio = null;
	/**
	 * Obtiene nuevo data source para rescatar los servicios.
	 * 
	 * @param servicio
	 *            El nombre del servicio.
	 * @returns {kendo.data.DataSource}
	 */
    
    /*function getDataSourceServicio(servicio) {
        return new bcs.DataSource({
            type : 'odata',
            definition : {
            	transport : {
                    read : {
                        url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=' + ID_VISTA_CONSULTA_SERVICIO,
                        dataType : 'json'
                    }
                },
                serverFiltering : true,
                serverSorting : true,
                filter : [ {
                    field : 'RutaServicio',
                    operator : 'endswith',
                    value : '.NOR::' + servicio
                }, {
                	field : 'TipoServicio',
                	operator : 'eq',
                	value : 'P'
                }],
                sort : {
                    field : 'IdServicio',
                    dir : 'ASC'
                },
            }
        });
    }*/
	
	function getDataSourceServicio(servicio) {
        return new kendo.data.DataSource({
            type : 'odata',
            sort : {
                field : 'IdServicio',
                dir : 'ASC'
            },
            transport : {
                read : {
                    url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=' + ID_VISTA_CONSULTA_SERVICIO,
                    dataType : 'json'
                }
            },
            serverFiltering : true,
            serverSorting : true,
            filter : [ {
                field : 'RutaServicio',
                operator : 'eq',
                value : 'BSCL.Normativa.BD.Servicios.NOR::' + servicio
            } ]
        });
    }
    
    /**
	 * Obtiene la URL del servicio asociado al esquema/metodo.
	 * 
	 * @param esquema
	 *            Nombre del esquema.
	 * @param args
	 *            Argumentos que se agregaran a la URL.
	 * @param success
	 *            Funcion a llamar en caso de exito.
	 * @param error
	 *            Funcion a llamar en caso de error.
	 */
    function getUrlServicio(servicio, success, error) {
        var dataSource = getDataSourceServicio(servicio);
        dataSource.fetch().then(function() {
            var servicios = dataSource.data();
            if (servicios.length < 1) {
                error({ message : 'No esta definido el servicio ' + servicio });
            } else {
            	_IdServicio = servicios[0].IdServicio;
                success();
            }
        });
    }
    
    /**
	 * Obtiene el data source para la consulta de los campos asociados al
	 * esquema/version.
	 * 
	 * @param esquema
	 *            El nombre del esquema.
	 * @param version
	 *            La version del esquema.
	 * @returns {kendo.data.DataSource}
	 */
    /*function getDataSourceCampo(esquema, version) {
        return new bcs.DataSource({
            type : 'odata',
            definition : {
            	transport : {
                    read : {
                    	url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=' + ID_VISTA_CONSULTA_CAMPOS,
                        dataType : 'json'
                    }
                },
                serverFiltering : true,
                serverSorting : true,
                filter : [ {
                    field : 'Esquema',
                    operator : 'eq',
                    value : esquema
                	}, {
                    field : 'Version',
                    operator : 'eq',
                    value : version
                	} ],
                sort : [ {
                    field : 'Grupo',
                    dir : 'ASC'
                	},  {
                    field : 'Orden',
                    dir : 'ASC'
                } ],
            }
        });
    }*/
    function getDataSourceCampo(esquema, version) {
        return new kendo.data.DataSource({
            type : 'odata',
            sort : [ {
                field : 'Grupo',
                dir : 'ASC'
            },  {
                field : 'Orden',
                dir : 'ASC'
            } ],
            transport : {
                read : {
                    url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=' + ID_VISTA_CONSULTA_CAMPOS,
                    dataType : 'json'
                }
            },
            serverFiltering : true,
            serverSorting : true,
            filter : [ {
                field : 'Esquema',
                operator : 'eq',
                value : esquema
            }, {
                field : 'Version',
                operator : 'eq',
                value : version
            } ]
        });
    }
    
    modelo.getDataSource = function(esquema, version, servicio, success, error) {
    	var dataSourceCampo = getDataSourceCampo(esquema, version);
    	dataSourceCampo.fetch().then(function(){
    		_campos =  null;
    		_columns = [];
    		_fields = {};
    		
    		_campos = dataSourceCampo.data();
    		if(_campos.length < 1) {
    			//error({message : 'No hay campos definidos para ' + esquema + '-' + version});
    		} else {
    			getUrlServicio(servicio, function() {
    	    		
    	        	var fieldMethods = new FieldMethods();
    	        	for (var i = 0; i < _campos.length; i++) {
    	        		
    	        		$.extend(_campos[i], fieldMethods)
    	        		
    	        		var campo = _campos[i];
    	        		var type = campo.getType();
    	        		
    	        		_fields[campo.Nombre] = { type : type };
    	        		_columns.push(getField(campo));
    	        	}
    	        	success();
    	    	}, 
    	    	error);
    		}
    	});
    }
    
    modelo.getDataSource2 = function(tablaParametros){
    	var filters = {};
    	filters.p_TablaParametros = tablaParametros;
    	return new bcs.DataSource ({
    		type : "call",
    		definition : {
    			serverFiltering 	: false,
                serverSorting 		: true,
                serverSorting 		: true,
                pageSize			: 25,
                callParameters		: filters,
                callTransaction		: _IdServicio,
                callResultSet		: "P_OUT_TABLASALIDA",
                schema				: {
                	model : {
                		fields		: _fields
                	}
                }
                
    		}
    	});
    }
    
    modelo.getColumns = function () {
    	return _columns;
    }
    /**
	 * Obtiene el campo en formato Grid.Field.
	 * 
	 * @param campo
	 *            El campo.
	 * @returns {___anonymous375_376}
	 */
    function getField(campo) {
        var c = {};
        c.field = campo.Nombre;
        c.title = campo.Titulo;
        c.format = campo.getFormat();
        c.width = campo.getWidth();
        c.attributes = campo.getAttributes();
        c.hidden = campo.isHiddenInGrid();
        return c;
    }
    
    return modelo;
}]);