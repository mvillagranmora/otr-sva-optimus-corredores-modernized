INSERT INTO "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
(
    "IdEventoCapitalCliente",           
    "IdEventoCapital",          
    "IdTipoEventoCapital",      
    "NumeroDividendo",
    "Secuencia",            
    "IdSocioNegocio",                   
    "TipoSocioNegocio",                 
    "IdPortafolio",             
    "IdTipoDocumentoIdentidad", 
    "DocumentoIdentidad",   
    "NombreCliente",                
    "IdClasificacionTipoInstrumento",   
    "IdTipoInstrumento",        
    "IdInstrumento",            
    "Nemo",                 
    "IdInstrumentoNuevo",
    "NemoNuevo",                        
    "FechaCierre",                      
    "FechaPago",                
    "CantidadDerecho",      
    "TipoCartera",                      
    "CantidadNueva",                    
    "MontoCapital",                     
    "MontoInteres",             
    "Total",                
    "IdMoneda",                     
    "SecuenciaCupon",                   
    "CodigoUsuarioAgente",              
    "NombreAgente",             
    "CodigoSucursal",       
    "FechaHoraIngresoRegistro",
    "FechaHoraUltimaActualizacion",     
    "IdUsuario",                        
    "IdEstado",
    "IdBoveda",
    "Origen",
    "IdBolsa",
    "IdTipoOperacionGarantia",
	"IdTipoReajuste",
	"CodigoCorredorContraparte",
	"TipoOperacion",
	"IdConceptoGarantia",
	"OperacionesVigentes",
	"Precio",
	"ISFUT",
	"RetencionISFUT",
	"MontoRetencionISFUT"
    )
VALUES(
    :p_IdEventoCapitalCliente,                                       --  IdEventoCapitalCliente
    :p_IdEventoCapital,                                              --  IdEventoCapital
    :p_IdTipoEventoCapital,                                          --  IdTipoEventoCapital
    :p_NumeroDividendo,                                              --  NumeroDividendo
    :p_Secuencia,                                                    --  Secuencia
    :p_IdSocioNegocio,                                               --  IdSocioNegocio
    :p_TipoSocioNegocio,                                             --  TipoSocioNegocio
    :p_IdPortafolio,                                                 --  IdPortafolio
    :p_IdTipoDocumentoIdentidad,                                     --  IdTipoDocumentoIdentidad
    :p_DocumentoIdentidad,                                           --  DocumentoIdentidad          
    :p_NombreCliente,                                                --  NombreCliente
    :p_IdClasificacionTipoInstrumento,                                 --  IdClasificacionTipoInstrumento
    :p_IdTipoInstrumento,                                            --  IdTipoInstrumento
    :p_IdInstrumento,                                                --  IdInstrumento
    :p_Nemo,                                                         --  Nemo
    :p_IdInstrumentoNuevo,                                           --  IdInstrumentoNuevo
    :p_NemoNuevo,                                                    --  NemoNuevo
    :p_FechaCierre,                                                  --  FechaCierre
    :p_FechaPago,                                                    --  FechaPago
    :p_CantidadDerecho,                                              --  CantidadDerecho
    :p_TipoCartera,                                                  --  TipoCartera
    :p_CantidadNueva,                                                --  CantidadNueva
    :p_MontoCapital,                                                 --  MontoCapital
    :p_MontoInteres,                                                 --  MontoInteres
    :p_Total,                                                        --  Total
    :p_IdMoneda,                                                     --  IdMoneda
    :p_SecuenciaCupon,                                               --  SecuenciaCupon
    :p_CodigoUsuarioAgente,                                          --  CodigoUsuarioAgente
    :p_NombreAgente,                                                 --  NombreAgente
    :p_CodigoSucursal,                                               --  CodigoSucursal
    :p_FechaHoraIngresoRegistro,                                     --  FechaHoraIngresoRegistro
    :p_FechaHoraUltimaActualizacion,                                 --  FechaHoraUltimaActualizacion
    :p_IdUsuario,                                                    --  IdUsuario
    :p_IdEstado,                                                     --  IdEstado
    :p_IdBoveda,                                                     --  IdBoveda
    :p_IdOrigen,                                                     --  Origen    
    :p_IdBolsa,                                                      --  IdBoveda
    :p_IdTipoOperacionGarantia,    
	0,
	'',
	'',
	'',
	:p_OperacionesVigentes,
	:p_Precio,													     -- Precio
	:p_ISFUT,														-- ISFUT
	:p_RetencionISFUT,												--RetencionISFUT
	:p_MontoRetencionISFUT											--MontoRetencionISFUT
    );                                                         
    
