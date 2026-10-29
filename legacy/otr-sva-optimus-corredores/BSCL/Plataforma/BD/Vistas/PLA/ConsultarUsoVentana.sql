SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarUsoVentana"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarUsoVentana"  AS 
	SELECT DISTINCT 
	    VEN."IdEmpresa", 
	    VEN."IdVentana",  
	    D."Descripcion",
	    VEN."IdUsuario", 
	    USU."CodigoUsuario", 
	    VEN."FechaAcceso", 
	    VEN."IdTipoCanalIngreso", 
	    TCA."Nombre" AS "NombreCanal", 
	    VEN."Adicional1" AS "Modulo"
	FROM "BSCL.Plataforma.BD::PLA.UsoVentana" AS VEN
	LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU ON (USU."IdUsuario" = VEN."IdUsuario") 
	LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" AS TCA ON (TCA."IdTipoCanalIngreso" = VEN."IdTipoCanalIngreso")
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Diccionario" AS D ON VEN."TituloVentana" = D."CodDiccionario" 
	WHERE 
		VEN."IdVentana" NOT IN ('VentanaMensajeResultado', 'VentanaResultado', 'NO_TITLE', 'Win_VentanaMensajeResultado', 'VentanaAlerta');

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarUsoVentana" TO  BCS_DESARROLLADOR WITH GRANT OPTION;