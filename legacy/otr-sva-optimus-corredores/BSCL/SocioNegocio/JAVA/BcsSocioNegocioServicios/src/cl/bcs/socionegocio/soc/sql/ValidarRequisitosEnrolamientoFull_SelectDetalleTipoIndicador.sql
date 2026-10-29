SELECT VCDTI."IdDetalleIndicador"
	 , VCDTI."IdTipoIndicador"
	 , VCDTI."Codigo"
	 , VCDTI."CodigoTipoIndicador"
	 , VCDTI."IdEmpresa"
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" VCDTI
WHERE VCDTI."IdEmpresa" = :IdEmpresa;