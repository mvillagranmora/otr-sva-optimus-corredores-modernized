SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.UAF::ConsultarComprobanteDOF";
GO
CREATE PROCEDURE 
--  Tecnologia : CrystalReports
--  Proposito: Servicio para consultar el comprobante de la declaracion de origenes de fondos.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 15/12/2016

"BSCL.Normativa.BD.Servicios.UAF::ConsultarComprobanteDOF" ( 
    --VARIABLES DEL PROCESO
 	IN p_IdEmpresa								BIGINT,
    IN p_Fecha									NVARCHAR(10)	DEFAULT '',--'YYYY-MM-DD'
	IN p_IdCliente								BIGINT			DEFAULT 0,
	--IN p_IdTipoOperacion 						NVARCHAR(6)		DEFAULT '',
	IN p_IdDeclaracionOrigenFondos					BIGINT			DEFAULT 0,
    IN p_Seccion								NVARCHAR(3) 	DEFAULT 'OPE' ---OPE(OPERACION/DOF(DECLARACION)
 
    --VARIABLES DE SALIDA 

)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		DECLARE v_Count 							 		 BIGINT:=0;
		DECLARE v_logo TABLE("LogoCorredora" BLOB);
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
		
		
		
IF	p_Seccion = 'OPE' OR p_Seccion ='' THEN
		CALL "BSCL.Normativa.BD.Servicios.UAF::ConsultarDOF"
		(	p_IdEmpresa 						=>:p_IdEmpresa,
			p_FechaOperacionDesde 				=>:p_Fecha,
			p_FechaOperacionHasta 				=>:p_Fecha,
			p_IdCliente 						=>:p_IdCliente,
			p_out_TablaDOF 						=>v_out_Tabla
		); 
		SELECT COUNT(1) INTO v_Count FROM  :v_out_Tabla;
		IF v_Count > 0 THEN
				SELECT *,'SI' AS "DATOS" FROM  :v_out_Tabla
				CROSS JOIN :v_datosGenerales
				CROSS JOIN :v_logo;		 
		ELSE
			SELECT 'NO' AS "DATOS" FROM  DUMMY;
		END IF;		
END IF;

IF	p_Seccion = 'DOF' THEN
	SELECT 
			DOF."IdDeclaracionOrigenFondos",
		 	DOF."IdCliente",
			DOF."IdTipoOperacionOrigenFondos",
			DOF."NombreTipoOperacionOrigenFondos",
		 	DOF."FechaDeclaracion",
		 	DOF."Comentario",
		 	DOF."IdUsuarioCreador",
		 	DOF."FechaIngreso",
		 	DOF."FechaCreacion",
		 	DOF."FechaModificacion",
		 	DOF."IdEmpresa",
		 	DOF."IdEstado",
			DOF."NombreEstado",
			DOF."OperacionInscripcion",
			--detalle
			DOF."DetalleExiste",
			DOF."IdTipoOrigenFondo",
			DOF."NombreTipoOrigenFondo",
			DOF."Valor",
			DOF."IdUsuarioCreadorDetalle",
			DOF."FechaIngresoDetalle",
			DOF."FechaCreacionDetalle",
			DOF."FechaModificacionDetalle", 
			DOF."IdEstadoDetalle",
			DOF."NombreEstadoDetalle"
	FROM 
	"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarDetalleDeclaracionOrigenFondos" DOF
	WHERE DOF."IdDeclaracionOrigenFondos" =:p_IdDeclaracionOrigenFondos
		AND DOF."Valor">0;
	/*					
	WHERE DOF."IdCliente" =:p_IdCliente AND TO_DATE(DOF."FechaDeclaracion")= TO_DATE(:p_Fecha)
						AND DOF."IdTipoOperacionOrigenFondos"=:p_IdTipoOperacion
						AND DOF."IdEmpresa"=:p_IdEmpresa
						AND DOF."Valor">0;
	*/
END IF;
		
		
END; 
