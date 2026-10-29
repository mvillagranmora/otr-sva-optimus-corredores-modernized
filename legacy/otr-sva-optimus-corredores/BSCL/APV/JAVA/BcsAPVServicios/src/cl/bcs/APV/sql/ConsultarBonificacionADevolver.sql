SELECT 
	 "IdBonificacion"			
	,Entidad."IdPersona" AS "IdPersonaEntidad"		
	,SUM(DetalleTraspaso."MontoTraspaso") AS "Monto"
	/*					
	,"IdBanco"		
	,"CuentaCorriente"	
	,*/
FROM "BSCL.APV.BD::INV.Bonificacion" Bonificacion
INNER JOIN "BSCL.APV.BD::TRA.Traspaso" Traspaso
	ON Bonificacion."IdCliente" = Traspaso."IdCliente"
	AND Bonificacion."AnoAhorro" = LEFT(Traspaso."FechaIngreso",4)
LEFT JOIN "BSCL.APV.BD::TRA.DetalleOrigenTraspaso" AS DetalleTraspaso
		ON DetalleTraspaso."IdTraspaso" = Traspaso."IdTraspaso"
		AND DetalleTraspaso."CodigoTipoAhorro" = 'APV'
		AND DetalleTraspaso."Fondo" = 'A'
INNER JOIN "BSCL.APV.BD.Vistas::SCS.ConsultarEntidadRecaudadora" Entidad
	ON Entidad."Nombre" = Traspaso."NombreEntidad"
--WHERE ("IdBonificacion" in (61))
WHERE ("IdBonificacion" in (:IdBonificacionGrupo))
	AND Bonificacion."IdEmpresa" = :IdEmpresa
GROUP BY "IdBonificacion"			
	,Entidad."IdPersona" 
