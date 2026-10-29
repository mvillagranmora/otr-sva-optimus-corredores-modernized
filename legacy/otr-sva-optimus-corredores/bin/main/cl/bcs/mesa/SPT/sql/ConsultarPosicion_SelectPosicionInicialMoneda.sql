SELECT
"IdPosicionInicial"
,"IdEmpresa"
,"Fecha"
,"IdMoneda"
,"TipoCambioInicial"
,"CierreIngresado"
,"PosicionInicial"
,"TipoInstrumento"
,"IdUsuario"
,"Estado"
,"ResultadoPosicionInicial"
FROM "BSCL.Mesa.BD.Vistas::ConsultarMonedasPosicionInicial"
WHERE "Estado" = 'ACT'
AND "IdEmpresa" = :IdEmpresa
AND TO_DATE("Fecha") BETWEEN :FechaOperacionDesde AND :FechaOperacionHasta;