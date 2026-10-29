SET SCHEMA "BSCL";
GO 
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.Anexos::ConsultarAnexoUno";
GO 
CREATE PROCEDURE 
--  Tecnologia : CrystalReports
--  Proposito: Servicio para consultar información para emitir el Anexo 1.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 25/07/2017

"BSCL.Normativa.BD.Servicios.Anexos::ConsultarAnexoUno" ( 

	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa			BIGINT,

    --VARIABLES DEL PROCESO
	IN	p_FechaOperacionDesde					NVARCHAR(10)	DEFAULT '',--'YYYY-MM-DD'
	IN	p_FechaOperacionHasta					NVARCHAR(10)	DEFAULT '',--'YYYY-MM-DD'
	IN	p_IdCliente 							BIGINT 			DEFAULT '0',

    --VARIABLES DE SALIDA OBLIGATORIAS

	OUT p_out_TablaAnexo1	"BSCL.Normativa.BD::NOR.SALIDA_ANEXO1"

)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_IdNormativa 								NVARCHAR(15):='ANEXO';
		DECLARE v_IdTipoDocumento   		 				NVARCHAR(10):='ACPE';
		DECLARE v_NombreDestinatario						NVARCHAR(100):='';
		DECLARE v_CargoDestinatario							NVARCHAR(40):='';				
		
  		DECLARE v_fechaHoraSistema 							 NVARCHAR(40):='';
  		DECLARE v_FechaOperacionDesde, v_FechaOperacionHasta NVARCHAR(10):='';
		DECLARE v_IdPersonaCtaPropia,v_IdClienteCtaPropia,v_Count ,v_IdTrabajoParidad		BIGINT:=0;

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
		SELECT   COALESCE(SOC."IdPersona",0),COALESCE(SOC."IdCliente",0) INTO v_IdPersonaCtaPropia,v_IdClienteCtaPropia
		FROM "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" as SOC 	
			INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP ON (SOC."IdSocioDeNegocio"=EMP."IdSocioDeNegocio")	
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON (CLI."IdPersona"=SOC."IdPersona" AND CLI."IdEmpresa"=EMP."IdEmpresa")
		WHERE EMP."IdEmpresa" = p_IdEmpresa AND EMP."IdEstado"='ACT';

	

		---- Transacciones desde servicionNormativo de Operaciones

		--v_TablaModulo = Select '' as "IdModulo" from dummy where 1=2;
		v_TablaModulo = Select 'FAC' as "IdModulo" from dummy;
		---Acotar busqueda servicio BASE
		v_registroParametro = SELECT 'FECHADESDE' AS "Codigo", v_FechaOperacionDesde AS "Valor" FROM DUMMY
					UNION ALL SELECT 'FECHAHASTA' AS "Codigo", v_FechaOperacionHasta AS "Valor" FROM DUMMY;
		
			CALL "BSCL.Normativa.BD.Servicios.NOR::ConsultarOperaciones"
			(
				p_IdEmpresa			=> p_IdEmpresa,
				p_IdNormativa		=> v_IdNormativa,
				p_IdTipoDocumento	=> v_IdTipoDocumento,
				p_tablaModulo		=> :v_TablaModulo,
				p_registroParametro	=> :v_registroParametro,
				p_out_TablaSalida	=> v_SalidaConsultarOperaciones
			);
		
		
		---- Transacciones 
		v_tablaOperaciones=
		SELECT
			ROW_NUMBER() OVER() AS "IdInterno",
			CUS."FechaMovimiento",
			CUS."IdOperacion",
			CUS."IdCliente",
			CUS."IdPortafolio",
			CUS."IdClasificacionTipoInstrumento",
			CUS."IdTipoInstrumento",
			CUS."IdTipoOperacion",
			CUS."IdMoneda",
			CUS."Monto",
			(CASE WHEN CUS."IdCliente"= v_IdClienteCtaPropia THEN 'S' ELSE 'N' END) AS "EsCtaPropia",
			COALESCE (IND."ValorIndicador",'N') AS "DerechoPrivilegiado",
			COALESCE (CDB."ValorIndicador",'N') AS "DerechoBolsa",
			CUS."AccionOperacion",
			CUS."HIdClasificacionTipoInstrumento"
		FROM  :v_SalidaConsultarOperaciones CUS
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC
						ON (SOC."IdCliente" =CUS."IdCliente")
		LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND  --DERECHOS PRIVILEGIADOS
						ON (IND."IdUso"='IND' AND IND."CodigoTipoIndicador"='DERPRIV' 
							AND IND."IdCliente" =SOC."IdCliente")
		LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" CDB  --CONVENIO DE PAGO DERECHOS BOLSA
						ON (CDB."IdUso"='IND' AND CDB."CodigoTipoIndicador"='DRCHCNV' 
							AND CDB."IdCliente" =SOC."IdCliente")
		WHERE 
		 (CUS."IdCliente" = p_IdCliente OR p_IdCliente= 0)
		 AND CUS."IdBolsa"='BCS'
		 ;
		
	
		--Verificar si existen operaciones para la conversion monetaria
		SELECT COUNT(1) INTO v_Count FROM :v_tablaOperaciones;
 				 
		IF v_Count > 0 THEN
		
		
			---IGUALAR PARIDADES			
						
			SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;
			
			INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
			SELECT :v_IdTrabajoParidad,CAR."IdInterno" AS "IdInterno",TO_VARCHAR (CAR."FechaMovimiento", 'YYYY-MM-DD') AS "Fecha", CAR."IdMoneda" as "IdMonedaOrigen", 'CLP' as "IdMonedaDestino",
			0 as "ValorCambioLocalModificado",CAR."Monto" as "MontoOrigen",'ORDENES' as "IdUso"
			FROM :v_tablaOperaciones CAR;

			call "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
				p_IdEmpresa => :p_IdEmpresa,
				p_IdTrabajo => :v_IdTrabajoParidad
			);
	
	
			v_out_Paridad=
					SELECT DISTINCT COALESCE(outt."MontoDestino",0) AS  "MontoDestino",outt."IdMonedaDestino", par.*
					FROM :v_tablaOperaciones par
					LEFT JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" outt 
					on par."IdInterno" = outt."IdInterno" AND outt."IdTrabajo" = :v_IdTrabajoParidad;
				
		--Tabla de Salida
			p_out_TablaAnexo1=
			SELECT 
				OPE."FechaMovimiento",
				OPE."IdOperacion",	
				OPE."IdCliente",
				OPE."IdPortafolio",
				OPE."IdClasificacionTipoInstrumento",	
				OPE."IdTipoInstrumento",	
				OPE."IdTipoOperacion",	
				OPE."IdMoneda",
				OPE."MontoCLP",
				OPE."Monto",
				OPE."EsCtaPropia",
				OPE."DerechoPrivilegiado",
				OPE."DerechoBolsa",
				OPE."AccionOperacion",
				OPE."HIdClasificacionTipoInstrumento"
			FROM	
			(			
	     			SELECT 
						OP."FechaMovimiento",
						OP."IdOperacion",	
						OP."IdCliente",
						OP."IdPortafolio",
						OP."IdClasificacionTipoInstrumento",	
						OP."IdTipoInstrumento",	
						OP."IdTipoOperacion",	
						OP."IdMoneda",
						"MontoDestino" AS "MontoCLP",
						OP."Monto",
						OP."EsCtaPropia",
						OP."DerechoPrivilegiado",
						OP."DerechoBolsa",
						OP."AccionOperacion",
						OP."HIdClasificacionTipoInstrumento"
	     			FROM :v_out_Paridad OP 
			) OPE 
			ORDER BY OPE."FechaMovimiento",OPE."IdCliente",OPE."IdOperacion";
				
		ELSE
		p_out_TablaAnexo1=SELECT
				'' AS "FechaMovimiento",
				0 AS "IdOperacion",	
				0 AS "IdCliente",
				0 AS "IdPortafolio",
				'' AS "IdClasificacionTipoInstrumento",	
				'' AS "IdTipoInstrumento",	
				'' AS "IdTipoOperacion",	
				'' AS "IdMoneda",
				0 AS "MontoCLP",
				0 AS "Monto",
				'' AS "EsCtaPropia",
				'' AS "DerechoPrivilegiado",
				'' AS "DerechoBolsa",
				'' AS "AccionOperacion",
				'' AS "HIdClasificacionTipoInstrumento"
				FROM DUMMY WHERE 1=2;


		
		END IF;
	SELECT * FROM :p_out_TablaAnexo1;			

END; 