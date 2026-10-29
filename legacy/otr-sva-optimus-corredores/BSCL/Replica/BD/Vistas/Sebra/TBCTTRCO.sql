SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTTRCO"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBCTTRCO" AS
	
		SELECT
      		CAST(CONF."Valor" AS VARCHAR(7)) AS COD_PER,
			COALESCE (CAST(CAR."FechaVencimientoOperacion" AS VARCHAR(100)),''   )  AS FEC_VCTO_TP, -- TIPO :char
			COALESCE (CAST(SUB."Nemotecnico" AS VARCHAR(100)),''   )  AS NEMO, -- TIPO :char
			COALESCE (CAST("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(CAST(IDENT."DocumentoIdentidad" AS VARCHAR(11))) AS VARCHAR(100)),'')  AS RUT_CLI, -- TIPO :char
			COALESCE (CAST(POR."CodigoPortafolio" AS VARCHAR(100)), '')  AS SEC_RUT_CLI, -- TIPO :decimal
			COALESCE (CAR."IdCartera" , 0    )  AS SEC_MOVTO, -- TIPO :decimal
			COALESCE (SUB."Cantidad" , 0    )  AS CANTIDAD, -- TIPO :decimal
			COALESCE (0 , 0    )  AS CANTIDAD_ACUM_VC, -- TIPO :decimal 
			COALESCE (CAST(COD_EMP_TRUN4."CodigoEmpleado" AS VARCHAR(100)),''   )  AS COD_AGENTE, -- TIPO :char
			COALESCE (TRAN."IdCorredorContraparte" , 0    )  AS COD_CORR_CONTRA, -- (listo) TIPO :decimal --  
			COALESCE (CAST(SUC."CodigoSucursal" AS VARCHAR(100)),''   )  AS COD_SUC, -- TIPO :char
			CASE CAR."IdEstado" WHEN 'ANT' THEN COALESCE (CAR."EGRFechaOperacion", '0001-01-01') ELSE '0001-01-01' END AS FEC_LIQ_ANTICIP,
			COALESCE (TO_VARCHAR(CAR."FechaLiquidacion" , 'YYYY-MM') ,''   )  AS FEC_LIQUID, -- TIPO :char
			COALESCE (TO_VARCHAR(TRAN."FechaOperacion"  , 'YYYY-MM-DD'),''   )  AS FEC_TRANS, -- TIPO :char  ---
			COALESCE (CAR."FolioComprobante" , 0    )  AS FOLIO_COMP_ADJ, -- TIPO :decimal
			COALESCE (CAST(LPAD(TRAN."Folio",8) AS VARCHAR(100)),''   )   AS FOLIO_TRANS, -- TIPO :char 
			COALESCE (CAST(CAR."IdBolsa" AS VARCHAR(3)),''   )  AS IND_BOLSA, -- TIPO :char
			''  AS IND_TIT_CUS, -- TIPO :char -- ver
			0  AS INTERES_DIA, -- TIPO :decimal
			0  AS LIN_COMP_ADJ, -- TIPO :decimal
			COALESCE (SUB."MontoSubyacente" , 0    )  AS MONTO, -- TIPO :decimal
			0  AS MONTO_ACUM_VC, -- TIPO :decimal --ver
			COALESCE (SUB."PrecioSubyacente" , 0    )  AS PRECIO, -- TIPO :decimal 
			0  AS SPREAD, -- TIPO :decimal
			COALESCE (CAR."TasaCompra" , 0    )  AS TASA, -- TIPO :decimal
			'FD'  AS TIPO_COMP_ADJ, -- TIPO :char 
			'V'  AS TIPO_OPERAC, -- TIPO :char --  JK > valores posibles V/C
			'N'  AS IND_FAC_PRIPRE, -- TIPO :char
			--COALESCE (CAST('' AS VARCHAR(100)),''   )  AS EST_PRESTATARIO, -- TIPO :char JK > valores posibles A anulado / L > liquidado /V  > vigente  /T  terminado
			CASE CAR."IdEstado" 
					WHEN 'ANU' THEN 'A' 
					WHEN 'LIQ' THEN 'L' 
					WHEN 'VIG' THEN 'V'
					WHEN 'VCD' THEN 'T'
					ELSE ''
					END AS EST_PRESTATARIO,
			'CL'  AS TIPO_PRESTATARIO, -- TIPO :char > CE  / CL  / CP  > CASE CO.TIPO_PRESTATARIO WHEN 'CE' THEN 'Venta Corta Externa' ELSE 'Venta Corta Interna' END   / cl > cliente / CP> cuenta propia
			COALESCE (CAST(MOV."IdOrden" AS VARCHAR(100)),''   )  AS FOLIO_ORDEN, -- 
			COALESCE (CAST(CAR."IdCondicionLiquidacion" AS VARCHAR(100)),''   )  AS COND_LIQ, -- TIPO :char > JK > homologar como en TBCTTRTP
			''  AS COD_OPERADOR, -- TIPO :char
			COALESCE (CAR."FolioCartera" , 0    )  AS FOLIO_PRESTAMO, -- TIPO :int 
			COALESCE (TO_VARCHAR(CAR."FechaOperacion"  , 'YYYY-MM-DD HH24:MI:SS.FF3'),'1900-01-01'   )  AS HORA_TRANS, -- TIPO :datetime JK fecha hora de la operacion ej: 2019-05-20 11:38:28.777
			COALESCE (CAR."PrecioAPlazo" , 0    )  AS PRIMA, -- TIPO :decimal --
			0  AS MONTO_COBRADO, -- TIPO :decimal -- ver
			0  AS MTO_VAL_HOY, -- TIPO :decimal
			''  AS FECHA_HOY, -- TIPO :char
			0  AS MTO_VAL_MAN, -- TIPO :decimal
			''  AS FECHA_MAN, -- TIPO :char
			COALESCE (SUB."INGCantidad" * "INGPrecioSubyacente", 0 ) AS MTO_VAL_MDO_HOY, -- TIPO :decimal 
			COALESCE (SUB."INGMontoCompraActualMonedaInstrumento", 0 )  AS MTO_VAL_MDO_MAN, -- TIPO :decimal 
			COALESCE (0 , 0    )  AS SEC_FAC, -- TIPO :decimal  
			COALESCE (0 , 0    )  AS SEC_FAC_PRIMA, -- TIPO :d
			COALESCE (CAST('N' AS VARCHAR(100)),''   )  AS IMPRESO, -- TIPO :char
			--COALESCE (CAST(CAR."IdTipoPrestamo" AS VARCHAR(100)),''   )  AS TIPOPRESTAMO, -- TIPO :char JK case co.TipoPrestamo  when 'VC' then 'Venta Corta'  when 'AD' then 'Administrativo'   WHEN 'OF' THEN case f.tipo_prestamo when 'C' then 'Otros (Fines Custodia)' else  'Otros (Fines Generales)' end                                      when ''   then 'Venta Corta'                                 end
			'VC' AS TIPOPRESTAMO, -- TIPO :char 
			COALESCE (CAST('' AS VARCHAR(100)),''   )  AS CODIGOPRESTAMO, -- TIPO :char
			COALESCE (0 , 0    )  AS SEC_FOLIO, -- TIPO :int
			'IN'  AS TIPO_REL, -- TIPO :char JK  / IN vigente / AN > anulado
			'N'  AS ACT_CARTERA -- TIPO :char
		FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" CONF ON "CodigoParametro" = 'PLA.FechaSistema'
		LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB ON CAR."FolioCartera" = SUB."FolioCarteraRelacionada" AND SUB."EsPrimeraCartera" = 'S'
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CAR."IdCliente" = CLI."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona" = PER."IdPersona"
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdCliente" = CLI."IdCliente" AND por."IdPortafolio"  = car."IdPortafolio"
		LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" COM ON COM."IdComprobante" = CAR."FolioComprobante"
		LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOV ON MOV."IdComprobante" = COM."IdComprobante"
			AND car."INGCantidad" = mov."Cantidad"
		LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" TRAN ON TRAN."IdTransaccion" = MOV."IdTransaccion"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" SUC ON SUC."IdSucursal" = CAR."IdSucursal"
		LEFT JOIN "BSCL.SocioNegocio.BD::EJE.Ejecutivo" as EJE ON EJE."IdEmpleadoEjecutivo" = POR."IdEmpleadoEjecutivo"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" as EMP ON EJE."IdEmpleadoEjecutivo" = EMP."IdEmpleado"
		LEFT JOIN (
			SELECT "IdEmpleado", CASE WHEN RANK > 1 THEN CONCAT(RPAD("CodigoEmpleado",3),RANK) ELSE "CodigoEmpleado" END AS "CodigoEmpleado" FROM (
				SELECT RANK() OVER (PARTITION BY RPAD("CodigoEmpleado",4) ORDER BY "CodigoEmpleado" ASC)  as RANK, "IdEmpleado", RPAD("CodigoEmpleado",4) AS "CodigoEmpleado" FROM "BSCL.SocioNegocio.BD::EMP.Empleado"
			) WHERE RANK < 10
		) AS COD_EMP_TRUN4 ON COD_EMP_TRUN4."IdEmpleado" = EMP."IdEmpleado"
		WHERE CAR."IdEstado" <> 'MOD' AND CAR."IdTipoInstrumento" = 'PRT' AND "IdTipoPrestamo" = 'PRTVTC'

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBCTTRCO" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
