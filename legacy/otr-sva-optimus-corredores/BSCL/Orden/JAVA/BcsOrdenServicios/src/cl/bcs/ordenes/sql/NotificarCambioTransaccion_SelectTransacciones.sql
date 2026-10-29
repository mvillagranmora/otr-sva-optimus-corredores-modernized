SELECT 
	 T."IdBolsa" 																		AS "Bolsa"
	,O."Nombre"																			AS "Tipo_Operacion" 
	,T."IdTransaccion"																	AS "IdTransaccion"
	,P."Nombre"				 															AS "Producto"
	,O."Accion"																			AS "Operacion"
	,O."Codigo"																			AS "Codigo_Operacion"
	,I."Nemotecnico" 																	AS "Instrumento"
	,CASE 
 		WHEN (O."Codigo" IN ('VENSIM','COMSIM')) THEN 
 			ISUB."Nemotecnico"
 		ELSE
 			I."Nemotecnico"
 	 END																				AS "Instrumento_Simultanea"
	,T."IdCondicionDeLiquidacion"														AS "Liquidacion"
	,CASE T."EsVentaCorta"
		WHEN 'S' THEN
			'Venta Corta'
		ELSE
			O."Nombre"
	END																					AS "Tipo_Transaccion"
	,"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(T."Cantidad",'i',0)				AS "Cantidad"
	,"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(T."Precio",'i',0)				AS "Precio"
	,"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(T."Monto",'i',0)				AS "Monto_Contado"
	,(SELECT ("BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(U."Monto",'i',0)) 
	  FROM 
		"BSCL.Orden.BD::ORD.Transaccion" U 
	  WHERE U."FechaOperacion" = T."FechaOperacion" 
	  AND T."IdTransaccionPlazo" = U."IdTransaccion" AND "EsAPlazo" = 'S')	AS "Monto_Compromiso"
	,T."Tasa"																			AS "Tasa"
	,T."FechaVencimiento" 																AS "Fecha_Vencimiento"
	,T."Folio"																			AS "Num_Transaccion"
	,COALESCE(C."IdPersona",0) 															AS "IDEMPLEADOPERSONA"
	,COALESCE(C."IdDireccionElectronicaPrincipal",0) 									AS "IDCORREOEMPLEADO"
	--,T."IdTransaccionPlazo" 															AS "Operador_Externo"
	,TO_VARCHAR(ADD_SECONDS(CURRENT_UTCTIMESTAMP,:SegundosZonaHoraria), 'DD-MM-YYYY') 	AS "FECHA"
	,TO_VARCHAR(ADD_SECONDS(CURRENT_UTCTIMESTAMP,:SegundosZonaHoraria), 'HH24:MI:SS') 	AS "HORA"
	,CC."RazonSocial" 																	AS "Corredora"
    ,CC."Telefono" 																		AS "Atencion_Cliente"
	
FROM "BSCL.Orden.BD::ORD.Transaccion" 												T
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" 									I 	ON T."IdInstrumento" 	= I."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" 									ISUB 	ON T."IdInstrumentoSubyacente" 	= ISUB."IdInstrumento"
INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion"					O 	ON T."IdTipoOperacion" 	= O."IdTipoOperacion" AND T."IdTipoInstrumento" = O."IdTipoInstrumento"
LEFT JOIN  "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado"  					C 	ON T."IdEmpleado" 		= C."IdEmpleado"
LEFT JOIN  "BSCL.Plataforma.BD::INS.TipoProducto"									P 	ON O."IdTipoProducto"	= p."IdTipoProducto"
INNER JOIN "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales"			CC 	ON CC."IdEmpresa" 		= :IdEmpresa

WHERE T."IdEmpresa" = :IdEmpresa AND T."IdTransaccion" IN ( :listaTransacciones )


