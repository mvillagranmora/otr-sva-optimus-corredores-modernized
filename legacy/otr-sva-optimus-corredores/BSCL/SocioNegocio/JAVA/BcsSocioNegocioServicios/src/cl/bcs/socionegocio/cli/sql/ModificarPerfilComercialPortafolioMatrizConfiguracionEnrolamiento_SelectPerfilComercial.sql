SELECT top 1  
	"IdPerfilComercialBase"
	  ,"IdPerfilProductos"
  FROM "BSCL.Plataforma.BD.Vistas::COM.ConsultarPerfilComercialBase" 
 WHERE "IdCategoriaInversionista" = :IdCategoriaInversionista
   AND "TipoEnrolamiento" = :TipoEnrolamiento
   AND "PorDefecto" = 'S';