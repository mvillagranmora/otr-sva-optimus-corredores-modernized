SELECT VCTR."IdTipoRelacion"
	 , VCTR."EsIndefinida"
	 , VCTR."UsaPorcentajeParticipacion"
	 , VCTR."UsaFinalidadRelacion"
	 , VCTR."UsaCondicionesRelacion"
	 , VCTR."UsaGradoVinculacionParental"
	 , VCTR."IdEstado"
FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacion" VCTR
WHERE VCTR."IdTipoRelacion" IN (:TipoRelacionIds);