UPSERT "BSCL.Plataforma.BD::PLA.ConfiguracionFlujoCaja"
(
	"IdUsoFlujoCaja",
	"ItemCalculo",
	"ItemOrigen",
    "Descripcion",
    "FuncionOperador",
    "IdEstado",
    "EsModificableUsuario"
 )
    
 SELECT  :IdUsoFlujoCaja,  
         :ItemCalculo, 
         :ItemOrigen, 
         :DescripcionConfiguracion, 
         :FuncionOperador, 
         :IdEstadoConfiguracion,
         :EsModificableUsuario
       
FROM DUMMY