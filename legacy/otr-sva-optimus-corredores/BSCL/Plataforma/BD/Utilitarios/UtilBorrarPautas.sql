SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Plataforma.BD.Utilitarios::UtilBorrarPautas";
CREATE PROCEDURE 
-- 
--  Proposito: Simplificar borrado de pautas (utilitario)
--  Autor: - JG - BCS
--  Fecha Creacion: 15/02/2017
--   
"BSCL.Plataforma.BD.Utilitarios::UtilBorrarPautas" (
	IN  p_IdEmpresa				BIGINT default 1,
	IN  p_IdTipoPrograma		NVARCHAR(40)
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
--READS SQL DATA
AS
BEGIN
	/***********************
		BORRAR EXISTENTE
	***********************/
	delete from "BSCL.Plataforma.BD::BAT.Programa" where "IdTipoPrograma" = :p_IdTipoPrograma;
	delete from "BSCL.Plataforma.BD::BAT.ParametrosPrograma" where "IdPrograma" in (
		select "IdPrograma" from "BSCL.Plataforma.BD::BAT.Programa" where "IdTipoPrograma" = :p_IdTipoPrograma
	);
	delete from "BSCL.Plataforma.BD::BAT.ParametrosAgenda" 
		where "IdAgenda" in (select "IdAgenda" from "BSCL.Plataforma.BD::BAT.Agenda" where "IdTipoPrograma"=:p_IdTipoPrograma);
	delete from "BSCL.Plataforma.BD::BAT.Agenda" where "IdTipoPrograma"=:p_IdTipoPrograma;
	delete from "BSCL.Plataforma.BD::BAT.Agenda" where "IdTipoPrograma" = :p_IdTipoPrograma;
	delete from "BSCL.Plataforma.BD::BAT.TipoPrograma" where "IdTipoPrograma" = :p_IdTipoPrograma;
	delete from "BSCL.Plataforma.BD::PAU.Dependencia" where "IdTipoPrograma" = :p_IdTipoPrograma and "Ejecucion" = 'PROGRAMADA';
	commit;
END;