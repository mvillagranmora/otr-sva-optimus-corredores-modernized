angular.module('ModuloPlataforma').factory('bcsPerfilComercial',function($filter,$q,$rootScope,bcsConfiguracionMatrizPerfilComercial,toaster,bcsComunes) {
	//$translate,toaster,$log,$uibModal,$ocLazyLoad,$stateParams,$rootScope
	var obj = {};


	obj.llamar = function(datos)
	{
		return('oli');
	};


	/*obj.generaInfoCobroOLD = function(value)
	{
		InfoCobro = '';
		if(value.IdValorCobro !== undefined) {
			InfoCobro = value.IdValorCobro + '\n['
									+ value.IdCobro
									+ '/'+ (value.FormaCobro === 'F' ? (value.IdMoneda==='UF' ? '' : '$') : '')
									+ $filter('kformat')(value.ValorCobro,"n2") + (value.FormaCobro === 'V' ? '%' : '')
									+'/'+value.ValorTopeMinimoOrden
									+'/'+value.IdMoneda +']';
		}
		return InfoCobro;
	};*/
	
	obj.generaInfoCobro = function(value)
	{
		InfoCobro = '';
		if(value.IdValorCobro !== undefined) {
			InfoCobro = '[' + value.IdCobro
							+ '/'+ (value.FormaCobro === 'F' ? (value.IdMoneda==='UF' ? '' : '$') : '')
							+ $filter('kformat')(value.ValorCobro,"n4") + (value.FormaCobro === 'V' ? '%' : '')
							+'/'+value.ValorTopeMinimoOrden
							+'/'+value.IdMoneda +']';
		}
		return InfoCobro;
	};

	obj.generaMatrizCobroCalculada = function(dataCobros)
	{
		var data = [];
		$.each(dataCobros,function(key,value){
			var aux = {};
			//0: todos.
			value.IdBolsa = (value.IdBolsa==='0') ? 'TODOS' : value.IdBolsa;
			value.IdTipoProducto = (value.IdTipoProducto==='0') ? 'TODOS' : value.IdTipoProducto;
			value.IdTipoOperacion = (value.IdTipoOperacion==='0') ? 'TODOS' : value.IdTipoOperacion;
			value.IdCanalIngreso = (value.IdCanalIngreso==='0') ? 'TODOS' : value.IdCanalIngreso;
			aux.IdBolsa = value.IdBolsa;
			aux.Corresponsal = 'N/A';
			aux.IdTipoProducto = value.IdTipoProducto;
			aux.IdTipoOperacion = value.IdTipoOperacion;
			aux.IdCanalIngreso = value.IdCanalIngreso;
			var infoCobro = obj.generaInfoCobro(value);
			aux.Comision = (value.CodigoCobroClasificacion === 'COMISION') ? infoCobro : '';
			aux.IdComision = (value.CodigoCobroClasificacion === 'COMISION') ? value.IdValorCobro : '';
			aux.ComisionIdDetallePerfilCobros = (value.CodigoCobroClasificacion === 'COMISION') ? value.IdDetallePerfilCobros : 0;
			aux.Derecho = (value.CodigoCobroClasificacion === 'DERECHO') ? infoCobro : '';
			aux.IdDerecho = (value.CodigoCobroClasificacion === 'DERECHO') ? value.IdValorCobro : '';
			aux.DerechoIdDetallePerfilCobros = (value.CodigoCobroClasificacion === 'DERECHO') ? value.IdDetallePerfilCobros : 0;
			aux.Gasto = (value.CodigoCobroClasificacion === 'GASTO') ? infoCobro : '';
			aux.IdGasto = (value.CodigoCobroClasificacion === 'GASTO') ? value.IdValorCobro : '';
			aux.GastoIdDetallePerfilCobros = (value.CodigoCobroClasificacion === 'GASTO') ? value.IdDetallePerfilCobros : 0;
			aux.Impuesto = (value.CodigoCobroClasificacion === 'IMPUESTO') ? infoCobro : '';
			aux.IdImpuesto = (value.CodigoCobroClasificacion === 'IMPUESTO') ? value.IdValorCobro : '';
			aux.ImpuestoIdDetallePerfilCobros = (value.CodigoCobroClasificacion === 'IMPUESTO') ? value.IdDetallePerfilCobros : 0;
			aux.Retencion = (value.CodigoCobroClasificacion === 'RETENCION') ? infoCobro : '';
			aux.IdRetencion = (value.CodigoCobroClasificacion === 'RETENCION') ? value.IdValorCobro : '';
			aux.RetencionIdDetallePerfilCobros = (value.CodigoCobroClasificacion === 'RETENCION') ? value.IdDetallePerfilCobros : 0;
			aux.Plazo = value.DiasPlazo;
			aux.Habilitado = value.EstadoDetalle;
			aux.Prioridad = parseInt(value.Prioridad);
			var llaveCompuestaPivote = value.IdBolsa + '_' + value.IdTipoProducto + '_' + value.IdTipoOperacion + '_' + value.IdCanalIngreso;
			var existeDato = false;
			$.each(data,function(key2,value2){
				var llaveCompuestaAux = value2.IdBolsa + '_' + value2.IdTipoProducto + '_' + value2.IdTipoOperacion + '_' + value2.IdCanalIngreso;
				if (llaveCompuestaPivote === llaveCompuestaAux) //console.log("Actualizacion de registros");
				{
					existeDato = true;
					data[key2].Comision = (aux.Comision !== '') ? aux.Comision : data[key2].Comision;
					data[key2].IdComision = (aux.IdComision !== '') ? aux.IdComision : data[key2].IdComision;
					data[key2].ComisionIdDetallePerfilCobros = (aux.ComisionIdDetallePerfilCobros !== 0) ? aux.ComisionIdDetallePerfilCobros : data[key2].ComisionIdDetallePerfilCobros;
					data[key2].Derecho = (aux.Derecho !== '') ? aux.Derecho : data[key2].Derecho;
					data[key2].IdDerecho = (aux.IdDerecho !== '') ? aux.IdDerecho : data[key2].IdDerecho;
					data[key2].DerechoIdDetallePerfilCobros = (aux.DerechoIdDetallePerfilCobros !== 0) ? aux.DerechoIdDetallePerfilCobros : data[key2].DerechoIdDetallePerfilCobros;
					data[key2].Gasto = (aux.Gasto !== '') ? aux.Gasto : data[key2].Gasto;
					data[key2].IdGasto = (aux.IdGasto !== '') ? aux.IdGasto : data[key2].IdGasto;
					data[key2].GastoIdDetallePerfilCobros = (aux.GastoIdDetallePerfilCobros !== 0) ? aux.GastoIdDetallePerfilCobros : data[key2].GastoIdDetallePerfilCobros;
					data[key2].Impuesto = (aux.Impuesto !== '') ? aux.Impuesto : data[key2].Impuesto;
					data[key2].IdImpuesto = (aux.IdImpuesto !== '') ? aux.IdImpuesto : data[key2].IdImpuesto;
					data[key2].ImpuestoIdDetallePerfilCobros = (aux.ImpuestoIdDetallePerfilCobros !== 0) ? aux.ImpuestoIdDetallePerfilCobros : data[key2].ImpuestoIdDetallePerfilCobros;
					data[key2].Retencion	= (aux.Retencion !== '') ? aux.Retencion : data[key2].Retencion;
					data[key2].IdRetencion	= (aux.IdRetencion !== '') ? aux.IdRetencion : data[key2].IdRetencion;
					data[key2].RetencionIdDetallePerfilCobros = (aux.RetencionIdDetallePerfilCobros !== 0) ? aux.RetencionIdDetallePerfilCobros : data[key2].RetencionIdDetallePerfilCobros;
				}
			});
			if(!existeDato){
				data.push(aux); //console.log("Ningun registro, se agrega el primero");
			}
		});
		return data;
	};


	obj.generaMatrizOperacional = function(DataPerfilProductos,DataPerfilCobros){
		var data = [];
		var dataCobroCalculada = obj.generaMatrizCobroCalculada(DataPerfilCobros);
		dataCobroCalculada = _.sortBy(dataCobroCalculada, 'Prioridad' ); // barrido por prioridad

		$.each(DataPerfilProductos,function(key0,value0){
			if(value0.EstadoProducto === 'ACT')
			{
				var aux = {};
				$.each(dataCobroCalculada,function(key1,value1){
					if(value0.IdTipoProducto === value1.IdTipoProducto &&
						(value0.IdTipoOperacion === value1.IdTipoOperacion || String(value0.IdTipoOperacion) === '0' || String(value1.IdTipoOperacion) === '0') &&
						(value0.IdCanalIngreso === value1.IdCanalIngreso || String(value0.IdCanalIngreso) === '0' || String(value1.IdCanalIngreso) === '0' ) &&
						value1.Habilitado  === 'ACT') // se verifican estados también!
					{
						// se hizo el match:
						var existeDato = false;
						$.each(data,function(key2,value2){
							if (value0.IdTipoProducto  === value2.NombreProducto  &&
									value0.IdTipoOperacion === value2.Operacion &&
									value0.IdCanalIngreso  === value2.Canal )
							{
								existeDato = true;
							}
						});
						if (!existeDato){
							aux.Bolsa = (value1.IdBolsa==='0')?'TODOS':value1.IdBolsa;
							aux.NombreProducto = value0.TipoProducto;
							aux.Operacion = (String(value1.IdTipoOperacion) !== 'TODOS' ) ? value1.IdTipoOperacion : value0.IdTipoOperacion;
							aux.Canal = (String(value1.IdCanalIngreso) !== 'TODOS' ) ? value1.IdCanalIngreso : value0.IdCanalIngreso;
							
							aux.Cobros =  ((value1.Comision==='' ) ? '' : (value1.Comision)
							 			+ ((value1.Derecho===''	 ) ? '' : (' ' + value1.Derecho))
							 			+ ((value1.Gasto===''	 ) ? '' : (' ' + value1.Gasto))
							 			+ ((value1.Impuesto==='' ) ? '' : (' ' + value1.Impuesto))
							 			+ ((value1.Retencion==='') ? '' : (' ' + value1.Retencion)));
							
							aux.Operacion = (aux.Operacion==='0')?'TODOS':aux.Operacion;
							aux.Canal = (aux.Canal==='0')?'TODOS':aux.Canal;
							data.push(aux);
						}
						else {console.log("Dato existe, pero se conserva anterior por prioridad");}
					}
				});
			}
			else
			{
				console.log("producto no habilitado");
			}
		});
		if (data.length>0) {
			data = _.sortBy(data, function(item) {
				 return [item.Bolsa+item.NombreProducto+item.Operacion+item.Canal];
			});// ordenar antes de retornar.
			return data;
		}
		else {
			bcsComunes.msg("warning","MATRIZ OPERACIONAL","Intersección de perfiles no genera datos en Matriz");
		}
	};

	obj.generaMatrizCobros = function(DataPerfilCobros){
		var data = obj.generaMatrizCobroCalculada(DataPerfilCobros);
		if (data.length>0) {
			data = _.sortBy(data, function(item) {
				 return [item.IdBolsa+item.IdTipoProducto+item.IdTipoOperacion+item.IdCanalIngreso];
			});// ordenar antes de enviar a grilla!
			return data;
		}
		else {
			bcsComunes.msg("warning","MATRIZ COBROS","No se pudo generar matriz");
		}
	};

	obj.generaListaValoresCobro = function(valoresCobro){
		var objDS = {};
		objDS.data= [];
		var nombreGrupo = '';
		var nombreSubGrupo = '';
		var tipoProductoEnLista = _.chain(valoresCobro).pluck('IdTipoProducto').unique().value();
		var CodigoCobroClasificacionEnLista = _.chain(valoresCobro).pluck('CodigoCobroClasificacion').unique().value();
		$.each(tipoProductoEnLista,function(key0,IdTipoProducto){
			if (nombreGrupo !== IdTipoProducto)
			{
				var menuA = {};
				menuA.items = [];
				menuA.text=IdTipoProducto;
				$.each(CodigoCobroClasificacionEnLista,function(key1,CodigoCobroClasificacion){
					if(nombreSubGrupo !== CodigoCobroClasificacion) {
						var menuN = {};
						menuN.items = [];
						menuN.text=CodigoCobroClasificacion;
						menuN.id=CodigoCobroClasificacion;
						$.each(valoresCobro,function(key1,value1){
							 if( IdTipoProducto === value1.IdTipoProducto && CodigoCobroClasificacion === value1.CodigoCobroClasificacion) {
									 var subMenuN = {};
									 subMenuN.text=value1.DescripcionValorCobro;
									 subMenuN.id=value1.IdValorCobro;
									 subMenuN.codigo=value1.IdValorCobro;
									 subMenuN.dataValorCobro=value1;
									 menuN.items.push(subMenuN);
							 }
						});
						menuA.items.push(menuN);
					}
					nombreSubGrupo = CodigoCobroClasificacion;
				});
				objDS.data.push(menuA);
			}
			nombreGrupo = IdTipoProducto;
		});
		return objDS;
	};

	obj.generaListaProductos = function(productos){
		var objDS = {};
		objDS.data= [];
		var nombreGrupo = '';
		$.each(productos,function(key0,producto){
			if (nombreGrupo !== producto.IdTipoProducto)
			{
				var menuN = {};
				menuN.text=producto.Nombre ;//.IdTipoProducto;
				menuN.id=producto.IdTipoProducto;
				menuN.dataProducto=producto;
				objDS.data.push(menuN);
			}
			nombreGrupo = producto.IdTipoProducto;
		});
		return objDS;
	};
	
	obj.generaListaOperaciones = function(operaciones){
		var objDS = {};
		objDS.data= [];
		var nombreGrupo = '';
		$.each(operaciones,function(key0,operacion){
			if (nombreGrupo !== operacion.IdOperacionUsuario)
			{
				var menuN = {};
				menuN.text			= operacion.Nombre ;//.IdOperacionUsuario;
				menuN.id			= operacion.IdOperacionUsuario;
				menuN.dataOperacion	= operacion;
				objDS.data.push(menuN);
			}
			nombreGrupo = operacion.IdOperacionUsuario;
		});
		return objDS;
	};
	
	obj.generaListaInstrumentos2 = function(instrumentos){
		var objDS = {};
		objDS.data= [];
		var nombreGrupo = '';
		$.each(instrumentos,function(key0,instrumento){
			if (nombreGrupo !== instrumento.IdInstrumento)
			{
				var menuN = {};
				menuN.text=instrumento.Nemotecnico ;// NombreTipoInstrumento
				menuN.id=instrumento.IdInstrumento;
				menuN.dataInstrumento=instrumento;
				objDS.data.push(menuN);
			}
			nombreGrupo = instrumento.IdTipoProducto;
		});
		return objDS;
	};
	
	obj.generaListaInstrumentos = function(instrumentos){
		var objDS = {};
		objDS.data= [];
		var nombreGrupo = '';
		var listaTipoInstrumento = _.chain(instrumentos).pluck('NombreTipoInstrumento').unique().value();
		console.log("listaTipoInstrumento", listaTipoInstrumento);
		$.each(listaTipoInstrumento,function(key0,NombreTipoInstrumento){
			if (nombreGrupo !== NombreTipoInstrumento)
			{
				var menuA = {};
				menuA.items = [];
				menuA.text=NombreTipoInstrumento;
				$.each(instrumentos,function(key1,value1){
					 if(NombreTipoInstrumento === value1.NombreTipoInstrumento) {
						 var subMenuN = {};
						 subMenuN.text=value1.Nemotecnico; //
						 subMenuN.id=value1.IdInstrumento;
						 subMenuN.dataInstrumento=value1;
						 menuA.items.push(subMenuN);
					 }
				});				
				objDS.data.push(menuA);
			}
			nombreGrupo = NombreTipoInstrumento;
		});
		return objDS;
	};


	return obj;
});
