SET SCHEMA "BSCL";  
 
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.ConsultarPuntas";

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.ConsultarPuntas" (
	-- Tabla Entrada
	IN IdMoneda NVARCHAR(3),
	IN IdEmpresa BIGINT,
	
	--Salida
   	OUT p_out_puntas table ( "IdPuntas"				BIGINT
   							,"IdEmpresa"			BIGINT
							,"Fecha" 				SECONDDATE
							,"IdUsuario"			NVARCHAR(50)
							,"AperturaPrecio"		DECIMAL(26,8)
							,"PuntaCompra"			DECIMAL(26,8)
							,"PuntaVenta"			DECIMAL(26,8)
							,"IdMonedaPunta"		NVARCHAR(3)
							,"CheckPrecioPorTramo"	NVARCHAR(2)
							,"Estado"				NVARCHAR(3)
							,"PrecioDecimales"		INTEGER
							,"MontoDecimales"		INTEGER
							,"TipoCambioDecimales"	INTEGER
							,"ParidadDecimales"		INTEGER
							,"PuntosDecimales"		INTEGER)
)
LANGUAGE SQLSCRIPT 
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
READS SQL DATA 
AS
BEGIN
	p_out_puntas =
		SELECT
			 TOP 1 PU."IdPuntas"
			,PU."IdEmpresa"
			,PU."Fecha"
			,PU."IdUsuario"
			,PU."AperturaPrecio"
			,PU."PuntaCompra"
			,PU."PuntaVenta"
			,PU."IdMonedaPunta"
			,PU."CheckPrecioPorTramo"
			,PU."Estado"
			,MF."PrecioDecimales"
			,MF."MontoDecimales"
			,MF."TipoCambioDecimales"
			,MF."ParidadDecimales"
			,MF."PuntosDecimales"
		FROM "BSCL.Mesa.BD::SPT.Puntas" PU
		--INNER JOIN :p_in_puntas AS PTA ON "BSCL.Mesa.BD::SPT.Puntas"."IdMonedaPunta" = PTA."IdMoneda"
		INNER JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF
			ON (PU."IdMonedaPunta" = MF."IdMoneda" AND MF."IdUsoMonedaFormato" = 'SPT' AND "IdEstado" = 'ACT')
		WHERE PU."Estado" = 'VIG'
		  AND PU."IdMonedaPunta" = :IdMoneda
		  AND PU."IdEmpresa" = :IdEmpresa
		ORDER BY PU."Fecha" DESC;

END;
