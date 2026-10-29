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
FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarPosicionInicial"
WHERE "IdEmpresa" = :IdEmpresa
AND "Estado" = 'ACT'
AND TO_DATE("Fecha") = :FechaOperacion;