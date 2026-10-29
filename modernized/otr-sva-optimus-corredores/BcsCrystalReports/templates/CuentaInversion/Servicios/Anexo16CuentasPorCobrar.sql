SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.CuentaInversion.CrystalReports.Servicios::Anexo16CuentasPorCobrar";
GO
--
--  Proposito: servicio para consultar ventas cortas para el Anexo 16 (normativa).
--  Autor: CFLORES - BCS
--  Fecha Creacion: 15/06/2017
--

CREATE PROCEDURE "BSCL.CuentaInversion.CrystalReports.Servicios::Anexo16CuentasPorCobrar"(
	-- Parametros entrada 
	IN p_IdEmpresa		BIGINT,
	IN p_FechaOperacion	DATE,
	IN p_IdUsuario		BIGINT
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER  
DEFAULT SCHEMA "BSCL"
--READS SQL DATA   
AS
BEGIN
	--------------------
	--Variables
	--------------------
	DECLARE v_logo TABLE("LogoCorredora" BLOB);
	DECLARE v_ExisteDolar		BIGINT := 0;
	DECLARE v_countOperaciones 	BIGINT := 0;
	DECLARE v_MensajeError		NVARCHAR(100) := '';
	DECLARE v_IdPersonaCtaPropia,v_Count,v_IdTrabajoParidad				BIGINT:=0;
	
	
	DECLARE v_FechaSistemaUTC   	NVARCHAR(40);
	DECLARE v_FechaSistemaLOCAL   	NVARCHAR(40);
	DECLARE v_FechaHoraSistemaUTC   NVARCHAR(40);
	DECLARE v_FechaHoraSistemaLOCAL SECONDDATE;
	DECLARE v_FechaHoraUTC   		NVARCHAR(40);
	DECLARE v_FechaHoraLOCAL   		NVARCHAR(40);
	/*
	DECLARE v_Movimientos TABLE(
				,"Correlativo" 			BIGINT
	 			,"IdCliente" 						BIGINT
				,"DocumentoIdentidadCliente"	 	NVARCHAR(40)
				,"NombreCliente"					NVARCHAR(100)
				,"MontoPorCobrar" 					DECIMAL(26,8)
				,"MontoHasta10" 					DECIMAL(26,8)
				,"MontoHasta30" 					DECIMAL(26,8)
				,"MontoMasDe30" 					DECIMAL(26,8)
				,"Observacion"  					NVARCHAR(100)
				,"ClasificacionContable"			NVARCHAR(100)
				,"Relacionado"  					NVARCHAR(1)
				,"FechaConsulta" 					NVARCHAR(10)
				,"FechaConsultaFormato" 			NVARCHAR(10)
				,"Usuario"							NVARCHAR(100)
	  			,"LogoCorredora" 					BLOB
				,"FechaSistemaLOCAL"  				NVARCHAR(10)
				,"HoraSistemaLOCAL"					NVARCHAR(5) 			
				,"MensajeError"	 					NVARCHAR(100)	
			);
	*/
	
	
	
		v_logo = select TOP 1
		"Binario" AS "LogoCorredora"
		from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;

		v_datosGenerales = select 
		"RazonSocial" AS "RazonCorredora", 
		"Direccion"   AS "DireccionCorredora", 
		"RUT" AS "RutCorredora", 
		"Telefono" AS "TelefonoCorredora"
		FROM "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales"
		WHERE "IdEmpresa" = p_IdEmpresa;
		
		--Valores de la cuenta propia	
	SELECT COALESCE(SOC."IdPersona",0) INTO v_IdPersonaCtaPropia
	FROM "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" as SOC 	
		INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP ON (SOC."IdSocioDeNegocio"=EMP."IdSocioDeNegocio")	
	WHERE EMP."IdEmpresa" = p_IdEmpresa AND EMP."IdEstado"='ACT';
	
	-- Se obtiene la misma FechaHoraSistema que se muestra en la UI
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(
		p_IdEmpresa					=> p_IdEmpresa,
		p_out_FechaSistemaUTC		=> v_FechaSistemaUTC,
		p_out_FechaSistemaLOCAL		=> v_FechaSistemaLOCAL,
		p_out_FechaHoraSistemaUTC	=> v_FechaHoraSistemaUTC,
		p_out_FechaHoraSistemaLOCAL	=> v_FechaHoraSistemaLOCAL,
		p_out_FechaHoraUTC			=> v_FechaHoraUTC,
		p_out_FechaHoraLOCAL		=> v_FechaHoraLOCAL
	);
	/*
	v_Clientes 		= SELECT "IdCliente","MontoPorCobrar"	FROM :v_Movimientos WHERE 1=2;
	v_MontoHasta10 	= SELECT "IdCliente","MontoHasta10" 		FROM :v_Movimientos WHERE 1=2;
	v_MontoHasta30 	= SELECT "IdCliente","MontoHasta30" 		FROM :v_Movimientos WHERE 1=2;
	v_MontoMasDe30 	= SELECT "IdCliente","MontoMasDe30" 		FROM :v_Movimientos WHERE 1=2;	
*/

	v_DetalleCobros=SELECT  ROW_NUMBER()  OVER (ORDER BY "IdCargoAbonoMovimiento")	AS "IdInterno"
					,"IdCargoAbonoMovimiento"
					,"FechaEfectivaMovimiento"
					,"IdCliente"
					,"DocumentoIdentidadCliente"
					,"NombreCliente"
					,"IdMoneda"
					,"Monto"
					,"AntiguedadDeuda"
					,"IdEstado"
					,"SaldoLibroInventario"
					FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentasPorPagarCobrar"
					WHERE "IdEstado"='CRT' AND TO_DATE("FechaEfectivaMovimiento") =:p_FechaOperacion;

					
		--Verificar si existen operaciones para la conversion monetaria
		SELECT COUNT(1) INTO v_Count FROM :v_DetalleCobros;
		IF v_Count = 0 THEN				
		
		v_MensajeError := 'NO_EXISTEN_OPERACIONES';
		
		--v_Movimientos =
			SELECT	
					'N' AS "Relacionado"
					, 0	AS "Secuencia"
			    	, 0 AS "IdCliente"
					,'' AS "FechaEfectivaMovimiento"
					,'' AS "DocumentoIdentidadCliente"
					,'' AS "NombreCliente"
					,0  AS "MontoTotal" --En Pesos
		 			,0  AS "MontoHasta10"
				    ,0  AS "MontoHasta30"
				    ,0  AS "MontoMayor30"
				    ,'' AS "FechaMovimientoFormato"
				    ,v_MensajeError	AS "MensajeError"
			FROM DUMMY
			CROSS JOIN :v_datosGenerales
			CROSS JOIN :v_logo;	
		
		ELSE
		---IGUALAR PARIDADES			
						
			SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;
			
			INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
			SELECT :v_IdTrabajoParidad,CAR."IdInterno" AS "IdInterno",TO_VARCHAR (CAR."FechaEfectivaMovimiento", 'YYYY-MM-DD') AS "Fecha", CAR."IdMoneda" as "IdMonedaOrigen", 'CLP' as "IdMonedaDestino",
			0 as "ValorCambioLocalModificado",CAR."Monto" as "MontoOrigen",'ORDENES' as "IdUso"
			FROM :v_DetalleCobros CAR;

			call "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
				p_IdEmpresa => :p_IdEmpresa,
				p_IdTrabajo => :v_IdTrabajoParidad
			);
	
	
			v_out_Paridad=
					SELECT DISTINCT COALESCE(outt."MontoDestino",0) AS  "MontoDestino",outt."IdMonedaDestino", par.*
					FROM :v_DetalleCobros par
					LEFT JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" outt 
					on par."IdInterno" = outt."IdInterno" AND outt."IdTrabajo" = :v_IdTrabajoParidad;
			---Resumen de clientes Monto total		
			v_ResumenClientes=
					SELECT 
						 "IdCliente"
						,"FechaEfectivaMovimiento"
						,"DocumentoIdentidadCliente"
						,"NombreCliente"
						,SUM("MontoDestino") AS "MontoTotal" --En Pesos
					FROM :v_out_Paridad
					GROUP BY "IdCliente","FechaEfectivaMovimiento","DocumentoIdentidadCliente","NombreCliente";
					
			v_ResumenHasta10=
					SELECT 
						 "IdCliente"
						,"FechaEfectivaMovimiento"
						,SUM("MontoDestino") AS "MontoHasta10" --En Pesos
					FROM :v_out_Paridad
					WHERE "AntiguedadDeuda" <=10 
					GROUP BY "IdCliente","FechaEfectivaMovimiento";
					
			v_ResumenHasta30=
					SELECT 
						 "IdCliente"
						,"FechaEfectivaMovimiento"
						,SUM("MontoDestino") AS "MontoHasta30" --En Pesos
					FROM :v_out_Paridad
					WHERE "AntiguedadDeuda" >10 AND "AntiguedadDeuda" <=30
					GROUP BY "IdCliente","FechaEfectivaMovimiento";
					
			v_ResumenMayor30=
					SELECT 
						 "IdCliente"
						,"FechaEfectivaMovimiento"
						,SUM("MontoDestino") AS "MontoMayor30" --En Pesos
					FROM :v_out_Paridad
					WHERE "AntiguedadDeuda" >30
					GROUP BY "IdCliente","FechaEfectivaMovimiento";
					
					
				
					
			---Tabla final compuesta		
				v_Movimientos =
					SELECT	RC.*
					    ,COALESCE(H10."MontoHasta10",0) AS "MontoHasta10"
					    ,COALESCE(H30."MontoHasta30",0) AS "MontoHasta30"
					    ,COALESCE(M30."MontoMayor30",0) AS "MontoMayor30"
					    ,TO_VARCHAR(RC."FechaEfectivaMovimiento",'DD/MM/YYYY') AS "FechaMovimientoFormato"
						FROM :v_ResumenClientes RC	
						LEFT OUTER JOIN :v_ResumenHasta10 H10 ON (H10."IdCliente"=RC."IdCliente" AND H10."FechaEfectivaMovimiento"=RC."FechaEfectivaMovimiento")
						LEFT OUTER JOIN :v_ResumenHasta30 H30 ON (H30."IdCliente"=RC."IdCliente" AND H30."FechaEfectivaMovimiento"=RC."FechaEfectivaMovimiento")
						LEFT OUTER JOIN :v_ResumenMayor30 M30 ON (M30."IdCliente"=RC."IdCliente" AND M30."FechaEfectivaMovimiento"=RC."FechaEfectivaMovimiento");
					
				
						
							SELECT ROW_NUMBER()  OVER (ORDER BY MOV."IdCliente")	AS "Secuencia" 
								,MOV.*,DT.*,LG.*
							    ,(CASE WHEN COALESCE(REL."IdPersonaDestino",0)=0 THEN 'N' ELSE 'S' END) AS "Relacionado"
								,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')	AS "FechaSistemaLOCAL"
								,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI')		AS "HoraSistemaLOCAL"
								,v_MensajeError	AS "MensajeError"
							FROM :v_Movimientos MOV
							INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI  ON (CLI."IdCliente"=MOV."IdCliente")
							LEFT OUTER JOIN 						 --RELACIONADOS CON LA CORREDORA
									(
											SELECT PER."IdPersonaDestino",PER."IdTipoRelacion",PER."NombreTipoRelacion" ,PER."Administracion"
											FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" PER
											WHERE PER."IdPersonaOrigen"=v_IdPersonaCtaPropia --Corredor
											AND PER."IdEstado"='ACT'
									) REL 
										ON (REL."IdPersonaDestino"=CLI."IdPersona")
							CROSS JOIN :v_datosGenerales DT
							CROSS JOIN :v_logo			LG	
							ORDER BY MOV."IdCliente";
												
		END IF;			
				
	

	
	
END;