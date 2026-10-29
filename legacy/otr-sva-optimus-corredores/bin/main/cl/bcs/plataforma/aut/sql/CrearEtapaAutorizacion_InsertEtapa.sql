INSERT INTO "BSCL.Plataforma.BD::AUT.Etapa"(
"IdEtapa",                                                              
"IdTipoEvento",
"Codigo",                                                                
"Nombre",
"Orden",                       
"TipoAutorizacion",
"MinimoAutorizan",
"IdEstado",          
"IdEmpresa"
)
VALUES (		
:IdEtapa,
:IdTipoEvento,
TRIM(:Codigo),    
TRIM(:Nombre),
:Orden,           
:TipoAutorizacion,     
:MinimoAutorizan,                 
:IdEstado,
:IdEmpresa                                                      	   	
);