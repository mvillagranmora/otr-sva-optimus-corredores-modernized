INSERT INTO "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio"
(                   
    "IdPreferenciaPagoPortafolio",
    "IdPortafolio",
    "IdPreferenciaPago",
    "IdCuentaBancaria",
    "IdTarjetaBancaria",
    "FechaCreacion",
    "FechaModificacion",
    "IdUsuarioCreador",
    "IdEstado",
    "IdEmpresa",            
    "FechaIngreso",                  
    "IdEstadoAutorizacion",
    "PorDefecto"
    ) 
SELECT 
    "BSCL.Portafolio.BD.Secuencias::PreferenciaPagoPortafolio".NEXTVAL,
    :IdPortafolio,
    PRE."IdPreferenciaPago",
    :IdCuentaBanco,
    0,
    CURRENT_UTCTIMESTAMP, --p_Creacion, -- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
    CURRENT_UTCTIMESTAMP,
    :IdUsuario,
    'ACT',
    :IdEmpresa,
    :FechaIngreso,
    'AUT',
    'S' 
FROM "BSCL.Plataforma.BD.Vistas::PAG.ConsultarPreferenciaPago" PRE
WHERE PRE."IdMedioDePago" = 'TRA' 
AND PRE."IdMoneda" = :MonedaSistema 
AND PRE."RecibePagaPortafolio" = 'P'
AND PRE."IdTipoProducto" NOT IN ('MEME','MERF','MEFW');	