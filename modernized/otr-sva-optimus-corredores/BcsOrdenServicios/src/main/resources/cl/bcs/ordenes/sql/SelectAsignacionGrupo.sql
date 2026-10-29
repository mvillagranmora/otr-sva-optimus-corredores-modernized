SELECT asigGrupo."IdAsignacion",asigGrupo."IdAsignacionGrupo",asigGrupo."EsAPlazo" 
FROM "BSCL.Orden.BD::ORD.Asignacion" asig
    INNER JOIN "BSCL.Orden.BD::ORD.Asignacion" asigGrupo ON (asigGrupo."IdAsignacionGrupo" = asig."IdAsignacionGrupo" AND asigGrupo."EsAPlazo" = 'N' AND asigGrupo."IdAsignacion" != asig."IdAsignacion")
WHERE asig."IdAsignacion" IN (:IdAsignacion) AND asig."IdAsignacionGrupo" > 0
