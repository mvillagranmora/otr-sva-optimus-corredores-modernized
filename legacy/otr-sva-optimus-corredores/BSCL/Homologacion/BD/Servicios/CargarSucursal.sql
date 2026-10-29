DROP PROCEDURE "BSCL"."BSCL.Homologacion.BD.Servicios::CargarSucursal";

CREATE PROCEDURE "BSCL"."BSCL.Homologacion.BD.Servicios::CargarSucursal"
 (
 IN  p_IdEmpresa			VARCHAR(40)
 ) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
BEGIN
		INSERT INTO "BSCL.Plataforma.BD::PLA.Sucursal"(
			 "IdSucursal"
			 ,"IdEmpresa"
			 ,"CodigoSucursal"
			 ,"NombreSucursal"
			 ,"EstadoSucursal" 
		)
		SELECT "BSCL.Plataforma.BD.Secuencias::Sucursal".NEXTVAL
		       ,p_IdEmpresa
		       ,SUC."suc_id"
		       ,SUC."suc_nombre"
		       ,'VIG'
		FROM "BSCL_VIRTUAL"."GES_SUCURSAL" as SUC
		WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Sucursal"
		                WHERE "IdEmpresa" = p_IdEmpresa
		                  AND "CodigoSucursal" = SUC."suc_id");
END;

