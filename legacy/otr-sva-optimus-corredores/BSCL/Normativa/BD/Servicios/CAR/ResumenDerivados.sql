SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.ResumenDerivados";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.ResumenDerivados" (
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
    
    v_ResumenDerivadosasdf =     
	SELECT
   		 P."IdCliente"
	   	,P."IdPortafolio"
	   	,'Derivados' as "grupo"
	   	,INS."NombreTipoInstrumento" as "item"
		,CAR."IdMonedaSistema"
		,TO_DECIMAL(SUM(CAR."INGMontoATasaCompraActual"),28,6) as "MontoMonedaSistema"	--Monto Valorizado a mercado en moneda de sistema "INGMontoATasaMercadoActual"
   	FROM :p_Datos AS P
   	INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldos" CAR ON CAR."IdPortafolio" = P."IdPortafolio" AND CAR."IdCliente" = P."IdCliente"
	INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS 
			ON INS."IdInstrumento" = CAR."IdInstrumento"
	WHERE INS."IdClasificacionTipoInstrumento" = 'DV'
	AND CAR."FechaOperacion" BETWEEN p_FechaDesde AND p_FechaHasta
	AND CAR."IdEstado" = 'VIG'
	GROUP BY P."IdCliente",P."IdPortafolio",INS."IdClasificacionTipoInstrumento",INS."NombreTipoInstrumento",CAR."IdMonedaSistema";
    
    v_ALGo  =
    SELECT T."IdCliente",T."IdPortafolio",T."grupo",T."item",T."MontoMonedaSistema" as "monto",G."total",0 as "totPorcActivo"
    FROM :v_ResumenDerivadosasdf  T
    INNER JOIN (SELECT SUM("MontoMonedaSistema") as "total", "IdCliente", "IdPortafolio", "grupo"
				FROM :v_ResumenDerivadosasdf
				GROUP BY "IdCliente", "IdPortafolio", "grupo") G 
			ON G."IdPortafolio" = T."IdPortafolio"
		   AND T."grupo" = G."grupo";
    

	SELECT * FROM :v_ALGo;
		   
 END;
GO