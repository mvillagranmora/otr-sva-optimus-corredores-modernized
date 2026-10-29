SET SCHEMA "BSCL";
GO 
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.ART171::ConsultarCartaUno";
GO 
CREATE PROCEDURE 
--  Tecnologia : CrystalReports
--  Proposito: Servicio para consultar información para emitir la Carta N°1 del Art. 171.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 25/07/2016

"BSCL.Normativa.BD.Servicios.ART171::ConsultarCartaUno" ( 

	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa			BIGINT,

    --VARIABLES DEL PROCESO
	IN	p_FechaOperacionDesde					NVARCHAR(10)	DEFAULT '',--'YYYY-MM-DD'
	IN	p_FechaOperacionHasta					NVARCHAR(10)	DEFAULT '',--'YYYY-MM-DD'
	IN	p_IdCliente 							BIGINT 			DEFAULT '0',

    --VARIABLES DE SALIDA OBLIGATORIAS

	OUT p_out_TablaCarta1	"BSCL.Normativa.BD::NOR.SALIDA_ART171CARTA1"

)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_IdNormativa 								NVARCHAR(15):='ART171CartaUno';
		DECLARE v_NombreDestinatario						NVARCHAR(100):='';
		DECLARE v_CargoDestinatario							NVARCHAR(40):='';				
		
		DECLARE v_ValorLimite								 DECIMAL (26,8):=0;		
  		DECLARE v_fechaHoraSistema 							 NVARCHAR(40):='';
  		DECLARE v_FechaOperacionDesde, v_FechaOperacionHasta NVARCHAR(10):='';
  		DECLARE v_VAL_MONTO_ART171, v_MON_MONTO_ART171 	 	 NVARCHAR(520):='';
		DECLARE v_IdPersonaCtaPropia,v_Count 				 BIGINT:=0;

		DECLARE v_out_error			  			 NVARCHAR(500);

	  	v_FechaOperacionDesde:=p_FechaOperacionDesde;
  		v_FechaOperacionHasta:=p_FechaOperacionHasta;  		
  
  		
		IF v_FechaOperacionDesde='' AND v_FechaOperacionHasta='' THEN
			call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
			);
			v_FechaOperacionDesde=TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD');
			v_FechaOperacionHasta=v_FechaOperacionDesde;
		END IF;

		IF v_FechaOperacionHasta='' THEN
			v_FechaOperacionHasta:=v_FechaOperacionDesde;  		
		END IF;
		
		IF v_FechaOperacionDesde='' THEN
			v_FechaOperacionDesde:=v_FechaOperacionHasta;  		
		END IF;

		---Parametros

		--Valores de la cuenta propia	
		SELECT COALESCE(SOC."IdPersona",0) INTO v_IdPersonaCtaPropia
		FROM "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" as SOC 	
			INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP ON (SOC."IdSocioDeNegocio"=EMP."IdSocioDeNegocio")	
		WHERE EMP."IdEmpresa" = p_IdEmpresa AND EMP."IdEstado"='ACT';

		--Destinatario

		SELECT "NombreDestinatario", "CargoDestinatario" INTO v_NombreDestinatario, v_CargoDestinatario
		FROM "BSCL.Normativa.BD::NOR.Normativa" WHERE "IdNormativa"=v_IdNormativa;

		---- Transacciones desde servicionNormativo de Operaciones

		v_TablaModulo = Select '' as "IdModulo" from dummy where 1=2;
		---Acotar busqueda servicio BASE
		v_registroParametro = SELECT 'FECHADESDE' AS "Codigo", v_FechaOperacionDesde AS "Valor" FROM DUMMY
					UNION ALL SELECT 'FECHAHASTA' AS "Codigo", v_FechaOperacionHasta AS "Valor" FROM DUMMY;
		
			CALL "BSCL.Normativa.BD.Servicios.NOR::ConsultarOperaciones"
			(
				p_IdEmpresa			=> p_IdEmpresa,
				p_IdNormativa		=> v_IdNormativa,
				p_tablaModulo		=> :v_TablaModulo,
				p_registroParametro	=> :v_registroParametro,
				p_out_TablaSalida	=> v_SalidaConsultarOperaciones
			);
		---Omitir operaciones con NOtas de Credito Asociadas		
		v_SalidaConsultarOperaciones = SELECT OP.* FROM :v_SalidaConsultarOperaciones OP
									LEFT  OUTER JOIN 
									(
										SELECT"IdComprobanteReferencia"
										FROM "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" NC
										WHERE NC."IdEstadoFacturacion"='FAC' AND NC."IdTipoComprobante"='NCE' AND NC."IdComprobanteReferencia">0
									) NC
									ON (OP."IdOperacion"=NC."IdComprobanteReferencia" ) 
										WHERE NC."IdComprobanteReferencia" IS NULL;
												
		---- Transacciones de Custodia		
		v_tablaOperaciones=
		SELECT
			CUS."FechaMovimiento" AS "FechaOperacion",
			CUS."IdOperacion",
			--CUS."NombreTipoOperacion", 	
			CUS."Nemotecnico", 			
			CUS."Cantidad",				
			CUS."Precio" 				AS "Precio",
			CUS."Monto" 			AS "Monto",
			CUS."IdMoneda"		 		AS "IdMoneda",
			--CUS."MonedaSistema" 		AS "Moneda",
			CUS."IdCliente",
			CUS."IdPortafolio",
			CUS."IdInstrumento",
			CUS."IdTipoProducto",
			CUS."IdClasificacionTipoInstrumento",
			CUS."IdTipoInstrumento",
			CUS."IdTipoOperacion",
			CUS."HIdTipoOperacion",
			REL."CodigoValorIndicador" AS  "IdTipoRelacion",
			REL."NombreValorIndicador" AS "NombreTipoRelacion"
		FROM  :v_SalidaConsultarOperaciones CUS
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC
						ON (SOC."IdCliente" =CUS."IdCliente")
		INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND  --INFORMACION PRIVILEGIADA
						ON (IND."IdUso"='IND' AND IND."CodigoTipoIndicador"='INFPRV' AND IND."ValorIndicador"='S'
							AND IND."IdCliente" =SOC."IdCliente")
		INNER JOIN "BSCL.Normativa.BD::NOR.Producto" NPRO 							 --PRODUCTO NORMATIVA
						 ON (NPRO."IdNormativa"=v_IdNormativa
						 AND CUS."IdTipoProducto"= NPRO."IdTipoProducto"
						 AND (CUS."IdClasificacionTipoInstrumento"	= NPRO."IdClasificacionTipoInstrumento" OR CUS."IdClasificacionTipoInstrumento"='') 
						 AND (CUS."IdTipoInstrumento" 				= NPRO."IdTipoInstrumento"  			OR CUS."IdTipoInstrumento"='') 
						 AND (CUS."IdTipoOperacion" 				= NPRO."IdTipoOperacion" 				OR CUS."IdTipoOperacion"=''))
						 --RELACIONADOS CON LA CORREDORA
/*
		INNER JOIN (
					SELECT PER."IdPersonaDestino",PER."IdTipoRelacion",PER."NombreTipoRelacion" ,PER."Administracion"
					FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" PER
					WHERE PER."IdPersonaOrigen"=v_IdPersonaCtaPropia --Corredor
					AND PER."IdEstado"='ACT'
					) REL 
					ON (REL."IdPersonaDestino"=SOC."IdPersona")
*/					 
		INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" REL
		ON (REL."IdUso"='IND' AND REL."CodigoTipoIndicador"='CI1237' AND  REL."CodigoValorIndicador" <>''
							AND REL."IdCliente" =SOC."IdCliente")
		
		WHERE 
		--CUS."FechaMovimiento" BETWEEN TO_DATE(v_FechaOperacionDesde) AND TO_DATE(v_FechaOperacionHasta)
		--AND
		 (CUS."IdCliente" = p_IdCliente OR p_IdCliente= 0);
		
	
		--Verificar si existen operaciones para la conversion monetaria
		SELECT COUNT(1) INTO v_Count FROM :v_tablaOperaciones;
 				 
		IF v_Count > 0 THEN

						
		--Tabla de Salida
			p_out_TablaCarta1=
			SELECT 
				v_NombreDestinatario AS "NombreDestinatario",
				v_CargoDestinatario AS "CargoDestinatario",
				OPE."FechaOperacion",
				OPE."IdOperacion",				
				OPE."HIdTipoOperacion" AS "IdTipoOperacion",
				OPE."NombreTipoOperacion", 	
				OPE."IdClasificacionTipoInstrumento",				
				OPE."IdInstrumento",				
				OPE."Nemotecnico", 			
				OPE."Cantidad",				
				OPE."Precio",
				OPE."Monto",
				OPE."IdMoneda",
				OPE."Moneda",
				OPE."IdCliente",
				OPE."NombreCliente",
				OPE."DocumentoIdentidad",
				OPE."TipoDocumentoIdentidad",
				OPE."NombreAbrTipoDocumentoIdentidad" AS "NombreTipoDocumentoIdentidad",
				OPE."IdTipoRelacion",
				OPE."NombreTipoRelacion" ,
				OPE."IdTipoRelacion" AS "CodigoRelacionNorma",
				OPE."NombreTipoRelacion" AS "DescripcionRelacionNorma"
			FROM	
			(			
	     			SELECT 
						SUBSTRING(OP."FechaOperacion",1,15) AS "FechaOperacion",
						OP."IdOperacion",
						OP."IdTipoOperacion",
						OP."HIdTipoOperacion",
						COALESCE(TPRO."Nombre",'') AS "NombreTipoOperacion", 	
						OP."Nemotecnico", 			
						OP."Cantidad" AS "Cantidad",				
						OP."Precio",
						OP."Monto"	AS "Monto",
						OP."IdMoneda",
						COALESCE(MON."Nombre",'')AS "Moneda",
						OP."IdCliente",
						CLI."NombreCompleto" AS "NombreCliente",
						--OP."IdPortafolio",
						OP."IdInstrumento",
						--OP."IdTipoProducto",
						OP."IdClasificacionTipoInstrumento",
						--OP."IdTipoInstrumento",
						CLI."DocumentoIdentidad" AS "DocumentoIdentidad",
						CLI."TipoDocumentoIdentidad",
						TIDEN."NombreAbrTipoDocumentoIdentidad",
						OP."IdTipoRelacion",
						OP."NombreTipoRelacion" 
	     			FROM :v_tablaOperaciones OP 
	     			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
	     				ON (OP."IdCliente"=CLI."IdCliente")
	     			INNER JOIN "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" as TIDEN ON  CLI."TipoDocumentoIdentidad" = TIDEN."IdTipoDocumentoIdentidad" 
     				LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TPRO	
					ON (OP."IdTipoOperacion"=TPRO."IdTipoOperacion")
	     			LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Moneda"  MON
	     			ON (OP."IdMoneda" = MON."IdMoneda") 	
					ORDER BY OP."IdCliente",OP."FechaOperacion",OP."Nemotecnico",OP."IdTipoOperacion" 
			) OPE 
			--LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::NOR.ConsultarRelacionNormativa" NOR
			--ON (OPE."IdTipoRelacion"=NOR."IdTipoRelacion" AND NOR."IdNormativa"=v_IdNormativa)
			ORDER BY OPE."FechaOperacion",OPE."IdCliente",OPE."IdOperacion";
				
		ELSE
		p_out_TablaCarta1=SELECT
				'' AS "NombreDestinatario",
				'' AS "CargoDestinatario", 
				'' AS "FechaOperacion",
				0 AS "IdOperacion",				
				'' AS "IdTipoOperacion",
				'' AS "NombreTipoOperacion", 	
				'' AS "IdClasificacionTipoInstrumento",				
				0 AS "IdInstrumento",				
				'' AS "Nemotecnico", 			
				0 AS "Cantidad",				
				0 AS "Precio",
				0 AS "Monto",
				'' AS "IdMoneda",
				'' AS "Moneda",				
				0 AS "IdCliente",
				'' AS "NombreCliente",
				'' AS "DocumentoIdentidad",
				'' AS "TipoDocumentoIdentidad",
				'' AS "NombreTipoDocumentoIdentidad",
				'' AS "IdTipoRelacion",
				'' AS "NombreTipoRelacion",
				'' AS "CodigoRelacionNorma",
				'' AS "DescripcionRelacionNorma"
				FROM DUMMY WHERE 1=2;	
		
		END IF;
	SELECT * FROM :p_out_TablaCarta1;			

END; 