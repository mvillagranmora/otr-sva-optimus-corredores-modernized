SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.SocioNegocio.BD.Servicios::SOC.ValidarDocumentoIdentidad";
GO
CREATE PROCEDURE
-- 
--  Proposito: Identificar si el documento de identidad a ingresar ya existe o esta asignado a otro socio de negocio
--  Autor: CHERNANDEZ - BCS
--  Fecha Creacion: 15-06-2017
--  
"BSCL.SocioNegocio.BD.Servicios::SOC.ValidarDocumentoIdentidad" (
	IN p_IdEmpresa					BIGINT,
	IN p_Entidad					NVARCHAR(15),--CLIENTE,EMPLEADO,PROVEEDOR,CONTRAPATE,PERSONA
    IN p_TipoDocumentoIdentidad		NVARCHAR(3),
	IN p_DocumentoIdentidad			NVARCHAR(40),
	IN p_TipoPersona				NVARCHAR(1),
	
	OUT p_out_IdPais				BIGINT,
	OUT p_out_valido 				NVARCHAR(1) 
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN	
	DECLARE v_existe BIGINT:=0;
	DECLARE p_out_CodigoPais NVARCHAR(3);
	
	SELECT CE."IdPais", P."CodigoPais" INTO p_out_IdPais, p_out_CodigoPais 
	FROM "BSCL.Plataforma.BD::REG.Pais"  P
	INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" CE ON CE."IdPais" = P."IdPais"
	WHERE CE."IdEmpresa" = p_IdEmpresa;
	
	IF p_out_CodigoPais = 'PE' AND p_TipoPersona <> 'J' THEN
		IF p_Entidad = 'PERSONA' THEN
			SELECT COUNT(1) INTO v_existe FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"
			WHERE "DocumentoIdentidad" = :p_DocumentoIdentidad AND "TipoDocumentoIdentidad" = :p_TipoDocumentoIdentidad
			AND "Tipo" = :p_TipoPersona;
		ELSEIF p_Entidad = 'CLIENTE' THEN
			SELECT COUNT(1) INTO v_existe FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"
			WHERE "DocumentoIdentidad" = :p_DocumentoIdentidad AND "TipoDocumentoIdentidad" = :p_TipoDocumentoIdentidad
			AND "Tipo" = :p_TipoPersona;
		ELSEIF p_Entidad = 'EMPLEADO' THEN
			SELECT COUNT(1) INTO v_existe FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado"
			WHERE "DocumentoIdentidad" = :p_DocumentoIdentidad AND "TipoDocumentoIdentidad" = :p_TipoDocumentoIdentidad
			AND "Tipo" = :p_TipoPersona;
		ELSEIF p_Entidad = 'CONTRAPARTE' THEN
			SELECT COUNT(1) INTO v_existe FROM "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte"
			WHERE "DocumentoIdentidad" = :p_DocumentoIdentidad AND "TipoDocumentoIdentidad" = :p_TipoDocumentoIdentidad
			AND "Tipo" = :p_TipoPersona;
		ELSEIF p_Entidad = 'PROVEEDOR' THEN
			SELECT COUNT(1) INTO v_existe FROM "BSCL.SocioNegocio.BD.Vistas::PRO.ConsultarProveedor"
			WHERE "DocumentoIdentidad" = :p_DocumentoIdentidad AND "TipoDocumentoIdentidad" = :p_TipoDocumentoIdentidad
			AND "Tipo" = :p_TipoPersona;
		END IF;
	ELSE
		IF p_Entidad = 'PERSONA' THEN
			SELECT COUNT(1) INTO v_existe FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"
			WHERE "DocumentoIdentidad" = :p_DocumentoIdentidad AND "TipoDocumentoIdentidad" = :p_TipoDocumentoIdentidad;
		ELSEIF p_Entidad = 'CLIENTE' THEN
			SELECT COUNT(1) INTO v_existe FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"
			WHERE "DocumentoIdentidad" = :p_DocumentoIdentidad AND "TipoDocumentoIdentidad" = :p_TipoDocumentoIdentidad;
		ELSEIF p_Entidad = 'EMPLEADO' THEN
			SELECT COUNT(1) INTO v_existe FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado"
			WHERE "DocumentoIdentidad" = :p_DocumentoIdentidad AND "TipoDocumentoIdentidad" = :p_TipoDocumentoIdentidad;
		ELSEIF p_Entidad = 'CONTRAPARTE' THEN
			SELECT COUNT(1) INTO v_existe FROM "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte"
			WHERE "DocumentoIdentidad" = :p_DocumentoIdentidad AND "TipoDocumentoIdentidad" = :p_TipoDocumentoIdentidad;
		ELSEIF p_Entidad = 'PROVEEDOR' THEN
			SELECT COUNT(1) INTO v_existe FROM "BSCL.SocioNegocio.BD.Vistas::PRO.ConsultarProveedor"
			WHERE "DocumentoIdentidad" = :p_DocumentoIdentidad AND "TipoDocumentoIdentidad" = :p_TipoDocumentoIdentidad;
		END IF;		
	END IF;
	
	IF v_existe > 0 THEN
		p_out_valido := 'N';
	ELSE
		p_out_valido := 'S';
	END IF;

END;