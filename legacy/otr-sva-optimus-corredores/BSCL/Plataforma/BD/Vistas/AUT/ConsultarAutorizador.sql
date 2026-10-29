SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::AUT.ConsultarAutorizador"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::AUT.ConsultarAutorizador"  AS
SELECT  
	ATE."IdAutorizadoEtapaEvento",
	EV."IdEtapaEvento",
	ATE."IdUsuario",
	COALESCE(USU."CodigoUsuario", '') AS "CodigoUsuario",
	ATE."IdRol",
    COALESCE(ROL."Nombre", '') AS "NombreRol",
    ATE."IdUsuarioReemplaza",
    COALESCE(USUREM."CodigoUsuario", '') AS "CodigoUsuarioReemplaza",
    ATE."ReemplazaDesde",
    ATE."ReemplazaHasta",
    CASE 
        WHEN E."IdEstado" <> 'VIG' AND ATE."IdEstado" = 'VIG' THEN 'OMITIDO' 
        ELSE EST."Nombre" 
    END AS "NombreEstado",
    ATE."FechaHoraCambioEstado",
    ATE."IdEstado",
    CASE WHEN ATE."Observaciones" = 'REEMPLAZO' 
    	 THEN 'SI' ELSE 'NO' 
  	END AS "ReemplazoAutoriza",
	CASE WHEN E."IdUsuarioSuperAutorizador" <> 0 
         AND (E."IdUsuarioSuperAutorizador" = ATE."IdUsuario"
         OR (ATE."Observaciones" = 'REEMPLAZO' AND E."IdUsuarioSuperAutorizador" = ATE."IdUsuarioReemplaza"))
    	 THEN 'SI' ELSE 'NO' 
	END AS "EsUsuarioSuperAutorizador",
    CASE WHEN E."IdRolSuperAutorizador" <> 0 AND E."IdRolSuperAutorizador" = ATE."IdRol"
    	 THEN 'SI' ELSE 'NO' 
    END AS "EsRolSuperAutorizador"
FROM "BSCL.Plataforma.BD::AUT.EtapaEvento" AS EV 
INNER JOIN "BSCL.Plataforma.BD::AUT.Evento" E 
	ON E."IdEvento" = EV."IdEvento"  
INNER JOIN "BSCL.Plataforma.BD::AUT.AutorizadoEtapaEvento" as ATE 
	ON ATE."IdEtapaEvento" =  EV."IdEtapaEvento" 
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST 
    ON ATE."IdEstado" = EST."IdEstado"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU  
    ON ATE."IdUsuario" = USU."IdUsuario"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USUREM  
    ON ATE."IdUsuarioReemplaza" = USUREM."IdUsuario"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Rol" AS ROL 
    ON ATE."IdRol" = ROL."IdRol";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::AUT.ConsultarAutorizador" TO  BCS_DESARROLLADOR WITH GRANT OPTION;