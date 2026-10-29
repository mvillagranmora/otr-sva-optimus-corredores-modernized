SET SCHEMA BSCL;

drop table "BSCL.Orden.BD::ORD.AsignacionPaso" cascade;
drop table "BSCL.Orden.BD::ORD.OrdenPaso" cascade;
drop table "BSCL.Orden.BD::ORD.TransaccionPaso" cascade;
drop table "BSCL.Orden.BD::ORD.AsignacionResultado" cascade;
   
  
 
 
  CREATE ROW TABLE "BSCL.Orden.BD::ORD.OrdenPaso" (
  	 "IdPrograma"								BIGINT			NOT NULL ,
 	 "IdOrdenPaso" 								BIGINT			NOT NULL ,
	 "IdClasificacionTipoInstrumento" 			NVARCHAR(3)		NOT NULL ,
	 "IdTipoInstrumento" 						NVARCHAR(6)		NOT NULL , 
	 "IdBolsa" 									NVARCHAR(3) 	NOT NULL ,
	 "IdTipoOperacion" 							NVARCHAR(6) 	NOT NULL , 
 	 "idCondicionDeLiquidacion" 				NVARCHAR(2)		NOT NULL ,
	 "Precio" 									DECIMAL(26,8)	NOT NULL ,
	 "CantidadDisponible"						DECIMAL(26,8)	NOT NULL ,
	 "CantidadAsignada"							DECIMAL(26,8)	NOT NULL ,
 	 "Monto" 									DECIMAL(26,8) 	NOT NULL ,
	 "IdTipoCanalDistribucion" 					NVARCHAR(3) 	NOT NULL ,
 	 "FechaIngreso"							 	TIMESTAMP		NOT NULL ,
	 "FechaOperacion" 							SECONDDATE		NOT NULL ,
	 "IdLibroOrdenes" 							INTEGER			NOT NULL ,
	 "IdTipoPrecio" 							NVARCHAR(1)				,
	 "IdTipoCanalIngreso"						NVARCHAR(3),
	 "EspecificaBolsa"							NVARCHAR(1)	,
	 "IdInstrumento" 							BIGINT
 );
 
  CREATE ROW TABLE "BSCL.Orden.BD::ORD.AsignacionPaso" (
  	 "IdPrograma" 				BIGINT			not null ,
 	 "IdAsignacionPaso" 		BIGINT			NOT NULL ,
	 "IdEstadoFacturacion" 		NVARCHAR(3)		NOT NULL ,
	 "CantidadAsignada" 		BIGINT			NOT NULL , --
	 "FechaAsignacion" 			SECONDDATE	 	NOT NULL ,
	 "IdTransaccionPaso" 		BIGINT 			NOT NULL , 
	 "IdOrdenPaso" 				BIGINT			NOT NULL

	 
	 
 );
 
 CREATE ROW TABLE "BSCL.Orden.BD::ORD.AsignacionResultado" (
 	 "IdPrograma" 				BIGINT			not null ,
 	 "IdAsignacionResultado"	BIGINT			NOT NULL ,
	 "IdEstadoFacturacion" 		NVARCHAR(3)		NOT NULL ,
	 "CantidadAsignada" 		BIGINT			NOT NULL , --
	 "FechaAsignacion" 			SECONDDATE	 	NOT NULL ,
	 "IdTransaccionPaso" 		BIGINT 			NOT NULL , 
	 "IdOrdenPaso" 				BIGINT			NOT NULL 
	 
 );
 
CREATE ROW TABLE "BSCL.Orden.BD::ORD.TransaccionPaso" (
	 "IdPrograma" 								BIGINT			not null ,
 	 "IdTransaccionPaso"						BIGINT			NOT NULL ,
	 "IdClasificacionTipoInstrumento" 			NVARCHAR(3)		NOT NULL ,
	 "IdTipoInstrumento" 						NVARCHAR(6)		NOT NULL , --
	 "IdBolsa" 									NVARCHAR(3) 	,
	 "IdTipoOperacion" 							NVARCHAR(6) 	, --
 	 "idCondicionDeLiquidacion" 				NVARCHAR(2)		,
	 "Precio" 									DECIMAL(26,8)	,
	 "CantidadTotal"							DECIMAL(26,8)	,
	 "CantidadDisponible"						DECIMAL(26,8)	,
	 "CantidadAsignada"							DECIMAL(26,8)	NOT NULL ,
	 "Monto" 									DECIMAL(26,8) 	,
 	 "FechaIngreso"							 	TIMESTAMP		,
 	 "FechaOperacion" 							SECONDDATE	,
 	 "IdLibroOrdenes"							BIGINT,
 	 "IdInstrumento"							BIGINT );
 	 
 	 
 	 
 /**	 
alter table "BSCL.Orden.BD::ORD.AsignacionResultado"
add ("IdPrograma" BIGINT);

alter table "BSCL.Orden.BD::ORD.AsignacionResultado"
add ("MontoAsignacion" BIGINT);
 	  	 
alter table "BSCL.Orden.BD::ORD.AsignacionPaso"
add ("IdPrograma" BIGINT);

alter table "BSCL.Orden.BD::ORD.TransaccionPaso"
add ("IdInstrumento" BIGINT);
 	 
alter table "BSCL.Orden.BD::ORD.OrdenPaso"
add ("IdInstrumento" BIGINT);
 	 
 	 */
 	  
