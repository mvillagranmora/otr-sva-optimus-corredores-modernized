SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.PLANTILLA_PRODUCTO"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.PLANTILLA_PRODUCTO" AS

	SELECT DISTINCT 
		 LEFT(PP."IdPerfilProductos",10)  AS PAR_TPP_ID
		,TCI."IdTipoCanalIngreso" AS CCA_CODIGO
		,COALESCE(right(COALESCE(H."ValorOrigen", DPP."IdTipoProducto"),2),'') 	 AS MERCADO 
		,LEFT(COALESCE(H."ValorOrigen", DPP."IdTipoProducto"),4)  AS PRO_ID
		,CASE WHEN PP."IdEstado" = 'ACT' THEN 'S' ELSE 'N' END AS DPP_IND_HABILITADO
		,'S' AS DPP_IND_RUTEO
		,'S' AS DPP_VAL_CTACTE
		,'S' AS DPP_VAL_CUSTODIA
		,'0' AS PAR_TIPO_LISTA_ID
		,'N' AS DPP_VAL_LINEA
		,'N' AS DPP_VAL_GARANTIA
	FROM "BSCL.Plataforma.BD::COM.PerfilProductos" PP
	INNER JOIN "BSCL.Plataforma.BD::COM.DetallePerfilProductos" DPP ON PP."IdPerfilProductos" = DPP."IdPerfilProductos" AND DPP."IdTipoProducto" NOT IN ('FACT', 'EVCA')
	LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" H ON H."ValorDestino" = DPP."IdTipoProducto" AND "TipoValor" = 'PlantillaProducto'
	INNER JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" TCI 
		ON TCI."IdTipoCanalIngreso" IN ('AGE', 'INT') AND 
		CASE 
			WHEN DPP."IdCanalIngreso" = '0' THEN TCI."IdTipoCanalIngreso" 
			WHEN DPP."IdCanalIngreso" <> 'INT' THEN 'AGE'
			ELSE DPP."IdCanalIngreso" 
		END = TCI."IdTipoCanalIngreso"
	WHERE PP."EsPerfilPersonalizado" = 'N'

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.PLANTILLA_PRODUCTO" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
