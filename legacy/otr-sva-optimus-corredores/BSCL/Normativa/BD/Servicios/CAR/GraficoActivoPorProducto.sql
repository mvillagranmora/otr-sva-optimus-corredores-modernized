SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.GraficoActivoPorProducto";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.GraficoActivoPorProducto" (
    IN p_IdModulo NVARCHAR(40),
    IN p_IdUsuario BIGINT,
    IN p_IdEmpresa BIGINT,
    IN p_FechaDesde DATE,
    IN p_FechaHasta DATE,
    IN p_Datos TABLE ("IdCliente" BIGINT, "IdPortafolio" BIGINT)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN

	CALL "BSCL.Normativa.BD.Servicios::CAR.Resumen_Inversion" (
		p_IdModulo				=> :p_IdModulo,
		p_IdUsuario				=> :p_IdUsuario,
		p_IdEmpresa				=> :p_IdEmpresa,
		p_FechaDesde			=> :p_FechaDesde,
		p_FechaHasta			=> :p_FechaHasta,
		p_IdTrabajo				=> 2,
		p_Datos					=> :p_Datos,
		p_out_ResumenInversion	=> v_ResumenInversion
	);
	
	SELECT "IdCliente","IdPortafolio","Grupo" as "grupo",MAX("TotalMonedaSistema") as "totPorcActivo"
	FROM :v_ResumenInversion
	GROUP BY "IdCliente","IdPortafolio","Grupo"
	ORDER BY "IdPortafolio";
END;
GO