SELECT "NombreGrupoCamara","Camaras" AS "Camara"
	,"IdEstado","IdCamaraDeCompensacion","Descripcion","CodigoGrupo"
FROM "BSCL.Plataforma.BD::CAM.GrupoCamaraDeCompensacion" 
WHERE "IdCamaraDeCompensacion" IN (:IdCamaraDeCompensacion) AND "Camaras" IN (:Camaras) AND "SecuenciaAgrupacion" = 'GRUPO3' 
	AND "IdEstado" = 'ACT';
	
