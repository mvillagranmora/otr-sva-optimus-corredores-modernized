SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.PatrimonioValorizado";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.PatrimonioValorizado" (
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
--READS SQL DATA
AS
BEGIN
	CALL "BSCL.Normativa.BD.Servicios::CAR.Resumen_Inversion" (
		p_IdModulo				=> :p_IdModulo,
		p_IdUsuario				=> :p_IdUsuario,
		p_IdEmpresa				=> :p_IdEmpresa,
		p_FechaDesde			=> :p_FechaDesde,
		p_FechaHasta			=> :p_FechaHasta,
		p_IdTrabajo				=> 3,
		p_Datos					=> :p_Datos,
		p_out_ResumenInversion	=> v_ResumenInversion
	);
	
	
		
	v_TotalInversion =
	SELECT "IdCliente","IdPortafolio","IdMonedaSistema",SUM("MontoMonedaSistema") as "total"
	FROM :v_ResumenInversion
	WHERE "Grupo" <> 'opl'
	GROUP BY "IdCliente","IdPortafolio","IdMonedaSistema";
	
	
	INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"
	SELECT -10, 1*"IdPortafolio", :p_FechaHasta, "IdMonedaSistema", "IdMonedaSistema", 0, "total",'CARTOLA' FROM :v_TotalInversion UNION
	SELECT -10, 2*"IdPortafolio", :p_FechaHasta, "IdMonedaSistema", 'UF', 0, "total",'CARTOLA' FROM :v_TotalInversion UNION
	SELECT -10, 3*"IdPortafolio", :p_FechaHasta, "IdMonedaSistema", 'USD', 0, "total",'CARTOLA' FROM :v_TotalInversion UNION
	SELECT -10, 4*"IdPortafolio", :p_FechaHasta, "IdMonedaSistema", 'EUR', 0, "total",'CARTOLA' FROM :v_TotalInversion;
	
	CALL "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
		p_IdEmpresa => :p_IdEmpresa,
		p_IdTrabajo => -10
	);
	
	SELECT   TI."IdCliente"
			,TI."IdPortafolio"
			,"BSCL.Plataforma.BD.Utilitarios::CamelCase"(MON."Nombre") || '(' ||MON."IdMoneda"||')'	 as "item"			
			,COALESCE(FIN."MontoDestino",0) as "monto"
			,'S' as "roundMont"
	FROM "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" AS FIN 
	LEFT  JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada" AS INI ON INI."IdInterno" = FIN."IdInterno"
	LEFT  JOIN :v_TotalInversion AS TI ON (1*TI."IdPortafolio" = FIN."IdInterno" OR 2*TI."IdPortafolio" = FIN."IdInterno" OR 3*TI."IdPortafolio" = FIN."IdInterno" OR 4*TI."IdPortafolio" = FIN."IdInterno")
	LEFT  JOIN "BSCL.Plataforma.BD::PLA.Moneda" as MON ON MON."IdMoneda" = FIN."IdMonedaDestino"
	WHERE FIN."IdTrabajo" = -10;
END;
GO
