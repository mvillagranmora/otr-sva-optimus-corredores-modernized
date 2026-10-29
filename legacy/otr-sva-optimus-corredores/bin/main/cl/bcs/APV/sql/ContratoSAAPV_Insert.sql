INSERT INTO  "BSCL.APV.BD::SCS.ContratoSAAPV"
( 
	"IdContratoSAAPV"				--BIGINT  		NOT NULL
	,"IdEmpresa"					--BIGINT 		NOT NULL
    ,"FolioContratoSAAPV" 			--NVARCHAR(50)	NOT NULL
    ,"IdPlan" 						--BIGINT		NOT NULL
	,"NombrePlan" 					--NVARCHAR(100)	NOT NULL
	,"FechaSuscripcion" 			--DATE			NOT NULL
	,"FechaCreacion" 				--DATE			
	,"FechaAprobacion" 				--DATE			
	,"FechaNotificacion"			--DATE			
	,"FechaRecepcion" 				--DATE			
	,"Folio"						--NVARCHAR(50)  NOT NULL
	,"IdPersona" 					--BIGINT 		NOT NULL
	,"IdEmpleador" 					--BIGINT  		NOT NULL
	,"IdCliente" 					--BIGINT  		NOT NULL
	,"NombreCliente"				--NVARCHAR(100)
	,"ApellidoPaternoCliente"		--NVARCHAR(100)
	,"ApellidoMaternoCliente"		--NVARCHAR(100)
	,"DocumentoIdentidadCliente"	--NVARCHAR(40)	NOT NULL
	,"FechaNacimientoCliente"		--DATE
	,"EstadoCivil"					--NVARCHAR(2)
	,"IdTipoGenero"					--NVARCHAR(2)	
	,"IdPaisResidenciaCliente"		--BIGINT
	,"IdCiudadCliente"				--BIGINT
	,"IdComunaCliente"
	,"DireccionCliente"				--NVARCHAR(100)
	,"IdTipoCargoPersona"			--BIGINT
	,"TelefonoCliente"				--NVARCHAR(40)
	,"TelefonoComercialCliente"		--NVARCHAR(40)
	,"TelefonoCelularCliente"		--NVARCHAR(40)
	,"EmailCliente"					--NVARCHAR(100)
	,"RazonSocialEmpleador"			--NVARCHAR(100)
	,"IdMedioRecaudacionTrabajador"	--BIGINT
	,"IdEntidadRecaudadora"         --BIGINT
	,"DocumentoIdentidadEmpleador"	--NVARCHAR(40)	NOT NULL
	,"DireccionComercialEmpleador"	--NVARCHAR(100)
	,"IdComunaEmpleador"			--BIGINT
	,"TelefonoEmpleador"			--NVARCHAR(40)
	,"NombreEncargado"				--NVARCHAR(100)
	,"EmailEncargado"				--NVARCHAR(100)
	,"TelefonoEncargado"			--NVARCHAR(40)
	,"CodigoPrevision"				--BIGINT		NOT NULL
	,"Pensionado"		        	--NVARCHAR(2) 	NOT NULL
	,"Trabajador"					--NVARCHAR(20)  NOT NULL
	,"FormaDePago"					--NVARCHAR(10)	NOT NULL
	,"IdMedioRecaudacionFormaPago"  --BIGINT		NOT NULL
    ,"DiaDescuento" 				--BIGINT
    ,"Institucion" 					--NVARCHAR(100)
	,"ComisionTransferencia" 		--DECIMAL(26,8)
	,"CodigoTipoSolicitud"			--NVARCHAR(3)
	,"NombreEntidadOrigen" 			--NVARCHAR(100)	NOT NULL
	,"RutEntidadOrigen"				--NVARCHAR(11)  NOT NULL
	,"TipoDeAhorro" 				--NVARCHAR(10)  NOT NULL
	,"FondoDeOrigen" 				--NVARCHAR(10)  NOT NULL
	,"TipoTraspaso"					--NVARCHAR(20) 	NOT NULL
	,"MontoTraspaso" 				--DECIMAL(26,8)
	,"UFTraspaso"					--DECIMAL(26,8)
	,"PorcentajeTraspaso"			--DECIMAL(26,8)
	,"IdEstadoEnvioNotificacion"	--NVARCHAR(3)
	,"IdEstadoSolicitud"			--NVARCHAR(3)
	,"IdEstado"						--NVARCHAR(3) 	NOT NULL
	,"IdCategoriaInversionista"		--NVARCHAR(40)  NOT NULL
	,"IdPerfilComercial"			--NVARCHAR(40)  NOT NULL
	,"IdCargaMasiva"
	,"Canal"						--NVARCHAR(100)
)   
values (
     :IdContratoSAAPV --"BSCL.APV.BD::SCS.IdContratoSAAPV".NEXTVAL
  	,:IdEmpresa
    ,:FolioContratoSAAPV 			
    ,:IdPlan 						
	,:NombrePlan 					
	,:FechaSuscripcion 				
	,:FechaCreacion 							
	,:FechaAprobacion					
	,:FechaNotificacion							
	,:FechaRecepcion 							
	,:Folio							
	,:IdPersona 					
	,:IdEmpleador 					
	,:IdCliente						
	,:NombreCliente					
	,:ApellidoPaternoCliente		
	,:ApellidoMaternoCliente		
	,:DocumentoIdentidadCliente		
	,:FechaNacimientoCliente		
	,:EstadoCivil					
	,:IdTipoGenero						
	,:IdPaisResidenciaCliente		
	,:IdCiudadCliente
	,:IdComunaCliente
	,:DireccionCliente				
	,:IdTipoCargoPersona			
	,:TelefonoCliente				
	,:TelefonoComercialCliente		
	,:TelefonoCelularCliente		
	,:EmailCliente					
	,:RazonSocialEmpleador	
	,:IdMedioRecaudacionTrabajador
	,:IdEntidadRecaudadora
	,:DocumentoIdentidadEmpleador	
	,:DireccionComercialEmpleador	
	,:IdComunaEmpleador				
	,:TelefonoEmpleador				
	,:NombreEncargado				
	,:EmailEncargado				
	,:TelefonoEncargado				
	,:CodigoPrevision				
	,:Pensionado		        	
	,:Trabajador	 			
	,:FormaDePago					
	,:IdMedioRecaudacionFormaPago		    
    ,:DiaDescuento	 				
    ,:Institucion 					
	,:ComisionTransferencia 
	,:CodigoTipoSolicitud
	,:NombreEntidadOrigen 			
	,:RutEntidadOrigen				
	,:TipoDeAhorro 					
	,:FondoDeOrigen 				
	,:TipoTraspaso					
	,:MontoTraspaso 				
	,:UFTraspaso					
	,:PorcentajeTraspaso
	,:IdEstadoEnvioNotificacion
	,:IdEstadoSolicitud
	,:IdEstado	
	,:IdCategoriaInversionista
	,:IdPerfilComercial
	,:IdCargaMasiva
	,:Canal
);
