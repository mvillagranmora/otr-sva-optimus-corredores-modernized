SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.OPERACION_RV"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.OPERACION_RV" AS

SELECT 
	ORD."IdOrden",
	COALESCE (ORD."IdOrden" , 0    )  AS "SOL_SEC", -- TIPO :numeric
	LEFT(COALESCE( hpr."ValorOrigen", 
				case when ORD."IdProducto"  = 'INSI' then 'AC'
						when ORD."IdProducto"  = 'INFU' then 'FU'
						when ORD."IdProducto"  = 'INEN' then 'EN'
						when ORD."IdProducto"  = 'INEX' then 'ET'
					 else  ORD."IdProducto"  END)
				
				, 2) AS "MERCADO", -- TIPO :char
	
	coalesce ("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(CAST( DOC."DocumentoIdentidad" AS VARCHAR(11))), '') AS "RUT_CLI", -- TIPO :varchar
	LEFT(COALESCE (ORD."Nemotecnico",''   ),16)  AS "ORV_NEMO", -- TIPO :char
	''  AS "ORV_CLASIFICACION_BOLSA", -- TIPO :char
	COALESCE (CAST('' AS VARCHAR(100)),''   )  AS "ORV_CLASIFICACION_CORREDORA", -- TIPO :char 
	COALESCE (ORD."Cantidad" , 0    )  AS "ORV_CANT_TOTAL", -- TIPO :numeric
	COALESCE (ORD."IdTipoPrecio",''   )  AS "ORV_TIPO_PRECIO", -- TIPO :char
	CASE WHEN ORD."IdTipoPrecio" = 'M' then 0 ELSE COALESCE (ORD."PrecioOrden" , 0) END  AS "ORV_VAL_TIPO_PRECIO", -- TIPO :numeric
	COALESCE (COB_OR."MontoComision" ,0)   AS "ORV_MTO_COMISION", -- TIPO :numeric
	COALESCE (COB_OR."MontoDerecho" ,0)  AS "ORV_MTO_DERECHOS", -- TIPO :numeric
	COALESCE (COB_OR."MontoGasto"  ,0) AS "ORV_MTO_GASTOS", -- TIPO :numeric
	ORD."MontoOrden"  AS "ORV_MTO_BRUTO", -- TIPO :numeric
	ORD."MontoOrden"  AS "ORV_MTO_TOTAL", -- TIPO :numeric
	COALESCE (0 , 0    )  AS "ORV_EFECTIVO_DISPONIBLE", -- TIPO :numeric
	ORD."PrecioOrden" AS "ORV_PRECIO_SUSCRIPCION", -- TIPO :numeric
	TO_VARCHAR (CASE WHEN ORD."FechaVencimiento" < '1900-01-01' THEN '1900-01-01' ELSE ORD."FechaVencimiento" end ,'YYYY-MM-DD') AS "ORV_FEC_VCTO", -- TIPO :datetime vencimiento simultanea 
	TO_VARCHAR (CASE WHEN ORD."FechaLiquidacion" < '1900-01-01' THEN '1900-01-01' ELSE ORD."FechaLiquidacion" END ,'YYYY-MM-DD')   AS "ORV_FEC_LIQUIDACION", -- TIPO :datetime FechaLiquidacion
	COALESCE (ORD."PrecioPlazo" , 0    )  AS "ORV_PRECIO_FINAL", -- TIPO :numeric PrecioPlazo
	coalesce (CASE WHEN locate(UPPER(TOPE."Nombre"), 'SIMUL') > 0 THEN ORD."PlazoSimultanea"
         WHEN locate(UPPER(TOPE."Nombre"), 'VENTA CORTA') > 0 THEN ORD."PlazoVentaCorta"		
	 END, 0)   AS "ORV_PLAZO", -- TIPO :smallint PlazoSimultanea or PlazoVentaCorta
	COALESCE (ORD."TasaSimultanea" , 0    )  AS "ORV_TASA_AGENTE", -- TIPO :numeric TasaSimultanea
	COALESCE (0 , 0    )  AS "ORV_SPREAD", -- TIPO :numeric x
	COALESCE (0 , 0    )  AS "ORV_UTILIDAD_CORREDORA", -- TIPO :numeric x
	COALESCE (ORD."MontoPrimaVentaCorta", 0    )  AS "ORV_MTO_PRIMA", -- TIPO :numeric
	coalesce (CASE WHEN locate(UPPER(TOPE."Nombre"), 'SIMUL') > 0 THEN ORD."TasaSimultanea"
         WHEN locate(UPPER(TOPE."Nombre"), 'VENTA CORTA') > 0 THEN ORD."TasaVentaCorta"		
	 END, 0)   AS "ORV_TASA_OPERACION", -- TIPO :numeric  TasaSimultanea or TasaVentaCorta
	COALESCE (0 , 0    )  AS "ORV_MTO_GARANTIA", -- TIPO :numeric x
	COALESCE (ORD."IdBoveda",''   )  AS "ORV_BOVEDA", -- TIPO :char
	COALESCE (ASIG."CantidadAsignada" , 0    )  AS "ORV_CAN_ASIGNAC_ACUM", -- TIPO :numeric 
	CASE WHEN coalesce (ORD."IdPorcentajeVisibilidad",100) = 0  then 100 ELSE ORD."IdPorcentajeVisibilidad" end /100.0 * ORD."Cantidad"   AS "ORV_CANT_VISIBLE", -- TIPO :numeric  IdPorcentajeVisibilidad del campo cantidad si fuese cero es 100 
	0.19  AS "ORV_VAL_IVA", -- TIPO :numeric  
	COALESCE (ORD."MontoLocalPlazo" , 0    )  AS "ORV_MTO_FINAL", -- TIPO :numeric MontoLocalPlazo
	TO_VARCHAR (CASE WHEN ORD."FechaOperacion" < '1900-01-01' THEN '1900-01-01' ELSE ORD."FechaOperacion" END ,'YYYY-MM-DD HH:MI:SS')   AS "ORV_FEC", -- TIPO :datetime FechaEmision
	TO_VARCHAR (CASE WHEN ORD."FechaVigencia" < '1900-01-01' THEN '1900-01-01' ELSE ORD."FechaVigencia" END ,'YYYY-MM-DD')  AS "ORV_FEC_PERMANENCIA", -- TIPO :datetime FechaVigencia
	CASE WHEN locate(UPPER(TOPE."Nombre"), 'SIMUL') > 0 then 'TS' when locate(UPPER(TOPE."Nombre"), 'VENTA CORTA') > 0 THEN 'VC'
		ELSE '' END  AS "ORV_IND_SIMUL", -- TIPO :char 
	CASE WHEN CL."Codigo" = 'CN' THEN '' ELSE CL."Codigo" END  AS "ORV_IND_LIQUID", -- TIPO :char
	COALESCE (ORD."IdBolsa",''   )  AS "ORV_BOLSA_DESTINO", -- TIPO :char idBolsa
	substr(TOPE."AccionOperacion",1,1)   AS "ORV_TIPO_OPERAC", -- TIPO :char 
	COALESCE (LEFT(EMP."CodigoEmpleado",4),''   )  AS "ORV_COD_AGE_DUE", -- TIPO :char ?????????????????
	COALESCE (ORD."IndicadorFacturacionInmediata",''   )  AS "ORV_IND_FACTURA_IMNEDIATA", -- TIPO :char IndicadorFacturacionInmediata
	COALESCE (ORD."OrdenVigente",''   )  AS "ORV_IND_VIGENTE", -- TIPO :char ORDENVIGENTE ?
	CASE WHEN ORD."IdTipoCanalDistribucion" != 'MAN' then 'S'ELSE 'N' END  AS "ORV_IND_PREF_RUTEO", -- TIPO :char
	COALESCE (CAST('' AS VARCHAR(100)),''   )  AS "ORV_CONCEPTO", -- TIPO :char
	COALESCE (CAST('' AS VARCHAR(100)),''   )  AS "ORV_TRA_BOVEDA", -- TIPO :char
	COALESCE (0 , 0    )  AS "ORV_TRA_FILIAL", -- TIPO :int
	COALESCE (0 , 0    )  AS "ORV_TRA_PERSONA", -- TIPO :int
	COALESCE (0 , 0    )  AS "ORV_TRA_CUENTA", -- TIPO :int
	COALESCE (0 , 0    )  AS "ORV_CAN_ASIGNAC_ACUM_INI", -- TIPO :numeric
	TO_VARCHAR (CASE WHEN ORD."FechaOferta" < '1900-01-01' THEN '1900-01-01' ELSE ORD."FechaOferta" END ,'YYYY-MM-DD HH:MI:SS') AS "ORV_FEC_CANTA", -- TIPO :datetime
	COALESCE (CAST('P' AS VARCHAR(100)),''   )  AS "ORV_TIP_MERCADO", -- TIPO :char
	COALESCE (CAST('N' AS VARCHAR(100)),''   )  AS "ORV_VENTA_MARTILLO", -- TIPO :char
	COALESCE (CAST('' AS VARCHAR(100)),''   )  AS "ORV_TIPO_ORDEN", -- TIPO :char
	COALESCE (0 , 0    )  AS "ORV_MTO_IMPUESTO", -- TIPO :numeric
	COALESCE (CAST('' AS VARCHAR(100)),''   )  AS "ORV_SPONSOR_FIRM", -- TIPO :char
	COALESCE (0 , 0    )  AS "ORV_SOL_SEC_OD", -- TIPO :numeric
	COALESCE (0 , 0    )  AS "ORV_DISPONIBLE_CUSTODIA", -- TIPO :decimal
	COALESCE(CAST(NEMONEG."Nemotecnico" AS CHAR(16)), '') AS "ORV_NEMO_NEGOCIABLE" -- TIPO :char

FROM "BSCL.Orden.BD::ORD.Orden" AS ORD 
LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS hpr ON (hpr."TipoValor" = 'TipoInstrumentoSegunMercado' 
                                                              AND hpr."Entidad" = 'TipoInstrumentoSegunMercado' 
                                                              AND hpr."ValorDestino" = ORD."IdTipoInstrumento"   AND hpr."ValorOrigen" NOT IN ('CM' , 'EN' , 'ET')  )
                                                              
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON (ORD."IdPortafolioCliente" = POR."IdPortafolio" )
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (ORD."IdCliente" = CLI."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = CLI."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON (PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad")
LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP ON (EMP."IdEmpleado" = ORD."IdEmpleadoPortafolio")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion"  TOPE ON  (TOPE."IdTipoOperacion" = ORD."IdTipoOperacion") 
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" CL ON  (CL."IdCondicionDeLiquidacion" = ORD."IdCondicionDeLiquidacion") 
LEFT JOIN (
			SELECT  CO_OR."IdOrden"  , sum( case when CO_CLA."CodigoCobroClasificacion" = 'COMISION' then co_or."MontoCobroCalculadoLocal"  else 0 end  ) as  "MontoComision" , 
									   sum( case when CO_CLA."CodigoCobroClasificacion" = 'GASTO'    then co_or."MontoCobroCalculadoLocal" else 0 end  ) as  "MontoGasto" , 
									   sum( case when CO_CLA."CodigoCobroClasificacion" = 'DERECHO'  then co_or."MontoCobroCalculadoLocal" else 0 end  ) as  "MontoDerecho", 
   									   sum( case when CO_CLA."CodigoCobroClasificacion" = 'IMPUESTO' then co_or."MontoCobroCalculadoLocal" else 0 end  ) as  "MontoIva" 
			FROM "BSCL.Orden.BD::ORD.CobroOrden"  as CO_OR
					JOIN "BSCL.Plataforma.BD::COM.Cobro" AS CO ON (CO_OR."IdCobro" = CO."IdCobro")
					JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" AS CO_CLA ON (CO."IdCobroClasificacion" = CO_CLA."IdCobroClasificacion")
			group by CO_OR."IdOrden"
		)
		AS COB_OR ON (ORD."IdOrden" = COB_OR."IdOrden")
LEFT JOIN (
		  SELECT  ASI."IdOrden", SUM( ASI."CantidadAsignada") AS  "CantidadAsignada"
		  FROM "BSCL.Orden.BD::ORD.Asignacion" as ASI
		  WHERE ASI."IdEstadoAsignacion" != 'ANU' AND ASI."EsAPlazo" = 'N'
		  GROUP BY ASI."IdOrden"
		  )
          as ASIG ON (ORD."IdOrden" = ASIG."IdOrden")
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS NEMONEG ON (ORD."IdInstrumentoNegociacion" = NEMONEG."IdInstrumento")
WHERE ORD."IdClasificacionTipoInstrumento" = 'RV' and TOPE."IdTipoOperacion" IS NOT NULL
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.OPERACION_RV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
