SELECT InstMot."IdMotivo" 
, InstMot."IdEstado" 
, InstMot."CodigoEstado"
, Est."Nombre" AS "Estado"
, InstMot."Motivo" 
, InstMot."Tipo" 
, InstMot."IdTipoCanalIngreso" 
, TipoCanalIngreso."Nombre" AS "Canal"
, InstMot."Activo" 
FROM "BSCL.Orden.BD::ORD.InstruccionMotivo" InstMot
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" Est 
	ON InstMot."IdEstado" = Est."IdEstado" 
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" TipoCanalIngreso 
	ON InstMot."IdTipoCanalIngreso" = TipoCanalIngreso."IdTipoCanalIngreso" 
LEFT JOIN "BSCL.Orden.BD::ORD.EstadoOrden" EstOrden
	ON InstMot."IdEstado" = EstOrden."IdEstado" and EstOrden."NombreEntidad" = 'INSTRUCCION'
WHERE (InstMot."IdEstado" = :p_IdEstado OR 1 = :var_IdEstado)
AND (InstMot."Motivo" = :p_Motivo OR 1 = :var_Motivo)
AND (InstMot."Tipo" = :p_Tipo OR 1 = :var_Tipo)
AND (InstMot."IdTipoCanalIngreso" = :p_IdTipoCanalIngreso OR 1 = :var_IdTipoCanalIngreso)
AND (InstMot."Activo" = :p_Activo OR 1 = :var_Activo)