SET SCHEMA "BSCL";
 
drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
	"IdPortafolio"			BIGINT,
  	"IdTipoOperacion"		NVARCHAR(6),
  	"IdBoveda"				NVARCHAR(3)
);

INSERT INTO #TT
SELECT 189419 AS "IdPortafolio",'VENSIM' AS "IdTipoOperacion",'DCV' AS "IdBoveda" from dummy;

SELECT * FROM "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Entrada_DatosGenerales";
SELECT * FROM "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Entrada_DatosInstrumentos";
SELECT * FROM "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Salida_PreciosTasasInstrumentos";
SELECT * FROM "BSCL.Plataforma.BD::VAL.Valorizar_Salida_InstrumentosValorizadosPQ"

SELECT "IdValorizacion","TipoCartera","UsoPrecio","IdTipoCriterioTasa",
                MAX(CASE WHEN "ValorizaACompra" = 'S' THEN "Precio" END) AS "PrecioCompra",
                MAX(CASE WHEN "ValorizaACompra" = 'S' THEN 0 END) AS "TasaCompra",
                MAX(CASE WHEN "ValorizaACompra" = 'S' THEN "Monto" END) AS "MontoCompra",       
                MAX(CASE WHEN "ValorizaAMercado" = 'S' THEN "PrecioMercado" END) AS "PrecioMercado",
                MAX(CASE WHEN "ValorizaAMercado" = 'S' THEN 0 END) AS "TasaMercado",
                MAX(CASE WHEN "ValorizaAMercado" = 'S' THEN "Monto" END) AS "MontoMercado"
             FROM "BSCL.Plataforma.BD::VAL.Valorizar_Salida_InstrumentosValorizadosPQ" as IVPQ
            GROUP BY "IdValorizacion","TipoCartera","UsoPrecio","IdTipoCriterioTasa"


select * 
      FROM "BSCL.Custodia.BD::CUS.DatosFlujoCustodia" as DFC
     INNER JOIN (SELECT "IdValorizacion","TipoCartera","UsoPrecio","IdTipoCriterioTasa",
                MAX(CASE WHEN "ValorizaACompra" = 'S' THEN "Precio" END) AS "PrecioCompra",
                MAX(CASE WHEN "ValorizaACompra" = 'S' THEN 0 END) AS "TasaCompra",
                MAX(CASE WHEN "ValorizaACompra" = 'S' THEN "Monto" END) AS "MontoCompra",       
                MAX(CASE WHEN "ValorizaAMercado" = 'S' THEN "PrecioMercado" END) AS "PrecioMercado",
                MAX(CASE WHEN "ValorizaAMercado" = 'S' THEN 0 END) AS "TasaMercado",
                MAX(CASE WHEN "ValorizaAMercado" = 'S' THEN "Monto" END) AS "MontoMercado"
             FROM "BSCL.Plataforma.BD::VAL.Valorizar_Salida_InstrumentosValorizadosPQ" as IVPQ
            GROUP BY "IdValorizacion","TipoCartera","UsoPrecio","IdTipoCriterioTasa"
            ) as IVPQ ON IVPQ."IdValorizacion" = DFC."IdDatosFlujoCustodia"
     WHERE DFC."KeyFlujoCustodia" = :p_KeyFlujoCustodia;

CALL "BSCL.Orden.BD.Servicios::ConsultarCustodiaIngresoOrden" (
	p_IdEmpresa    => 1,
	p_IdUsuario    => 25,
	p_IdModulo    => 'ORD',
	p_FechaSaldo    => '2017-06-09',
	p_DiasProyeccion    => 0,
	p_Ordenes => #TT,
	
	p_out_Salida    => ?,
	p_out_ErroresDetalle =>?
) ;

COMMIT
rollback


--VENIIF
--VENSIM
--VENACN

SELECT * FROM "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacion"

SELECT * FROM "BSCL.Custodia.BD::CUS.DatosFlujoCustodia" WHERE "KeyFlujoCustodia" = 167 and "TipoRegistro" <> 'M'



