CALL "BSCL.FondoMutuo.BD.Servicios::ObtenerDatosInformeRentabilidadFFMMExtranjera" (
    p_IdEmpresa => :IdEmpresa,
    p_EsMasivo => :EsMasivo,
    p_AgnoComercial => :AgnoComercial,
    p_IdTrabajo => :IdTrabajo,
    
    p_salida => $p_salida
);