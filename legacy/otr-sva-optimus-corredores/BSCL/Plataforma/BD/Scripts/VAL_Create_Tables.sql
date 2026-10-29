set schema "BSCL";

drop TYPE "BSCL.Plataforma.BD::VAL.InstrumentosValorizadosRV" CASCADE;
drop TYPE "BSCL.Plataforma.BD::VAL.InstrumentosValorizadosRF" CASCADE;
drop TYPE "BSCL.Plataforma.BD::VAL.InteresesCalculados" CASCADE;


drop table "BSCL.Plataforma.BD::VAL.InstrumentosPQValorizaCustodia";
drop table "BSCL.Plataforma.BD::VAL.InstrumentosWSValorizaCustodia";
drop table "BSCL.Plataforma.BD::VAL.ProcesamientoPQValorizaCustodia";
drop table "BSCL.Plataforma.BD::VAL.ProcesamientoWSValorizaCustodia";


/* OLD
set schema bscl;
create table VAL_VALJOBS(
	GRUPO  		bigint,
	PROGRAMA  	bigint,
	TAREA 		bigint,
	ORDEN 		bigint,
	OPCION 		nvarchar(4),
	TIPOUNIDAD 	int	
);
set schema bscl;
create table TIP_VALJOBS(
	GRUPO  		bigint,
	PROGRAMA  	bigint,
	TAREA 		bigint,
	ORDEN 		bigint,
	OPCION 		nvarchar(4),
	TIPOUNIDAD 	int	
);
*/


set schema bscl;
drop table VAL_VALJOBS;
drop table "BSCL.Plataforma.BD::VAL.VAL_VALJOBS";
create table "BSCL.Plataforma.BD::VAL.VAL_VALJOBS"(
	GRUPO  		bigint,
	PROGRAMA  	bigint,
	TAREA 		bigint,
	ORDEN 		bigint,
	OPCION 		nvarchar(4),
	TIPOUNIDAD 	int	
);

drop table TIP_VALJOBS;
drop table "BSCL.Plataforma.BD::VAL.TIP_VALJOBS";
create table "BSCL.Plataforma.BD::VAL.TIP_VALJOBS"(
	GRUPO  		bigint,
	PROGRAMA  	bigint,
	TAREA 		bigint,
	ORDEN 		bigint,
	OPCION 		nvarchar(4),
	TIPOUNIDAD 	int	
);






--###################################
-- BORRADO INICIAL SCRIPT
--###################################
drop table "BSCL.Plataforma.BD::VAL.Depuracion";
drop table "BSCL.Plataforma.BD::VAL.ProcesamientoCupon";

drop sequence "BSCL.Plataforma.BD.Secuencias::IdProcesamientoCupon";
drop sequence "BSCL.Plataforma.BD.Secuencias::TicketProcesamientoCupon";
drop sequence "BSCL.Plataforma.BD.Secuencias::IdInstrumentoValorizaCustodia"; 
drop sequence "BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodia"; 
drop sequence "BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodiaMasiva"; 
drop sequence "BSCL.Plataforma.BD.Secuencias::TicketProcesamientoValorizaCustodia";

--###################################
-- TABLAS AUXILIARES/CONTROL SP's
--###################################

-- select * from "BSCL.Plataforma.BD::VAL.Depuracion" order by  "FechaHora" desc;
-- select count(1) from "BSCL.Plataforma.BD::VAL.ProcesamientoPQValorizaCustodia" where "TicketProcesamiento" = 266;
-- select count(1) from "BSCL.Plataforma.BD::VAL.ProcesamientoWSValorizaCustodia" where "TicketProcesamiento" = 266;

create column table "BSCL.Plataforma.BD::VAL.Depuracion"(
	 "IdProceso" 		  	NVARCHAR(500),
	 "ValorizaPQ"		  	NVARCHAR(1),
	 "CantidadPQ"		  	bigint,
	 "ValorizaWS" 		  	NVARCHAR(1),
	 "CantidadWS" 		  	BIGINT,
 	 "TiempoInicio" 	  	TIME,
 	 "TiempoFin" 	  	  	TIME,
 	 "Diferencia"		  	DECIMAL(26,8),
 	 "FechaHora"		  	TIMESTAMP,
 	 "TicketProcesamiento"	BIGINT,
 	 "OrigenProcesamiento"	NVARCHAR(40)
);

-- SP: "BSCL.Plataforma.BD.Servicios::VAL.Cupon"
set schema bscl;
drop table  "BSCL.Plataforma.BD::VAL.ProcesamientoCupon";
create column table "BSCL.Plataforma.BD::VAL.ProcesamientoCupon"(
	"IdProcesamientoCupon"		BIGINT,
	"TicketProcesamiento"		BIGINT,	
	-- DATOS TAREA	
	"IdTarea" 					BIGINT, -- "BSCL.Plataforma.BD.Secuencias::INS.TareaValorizacion".NEXTVAL				 	
	-- DATOS ENTRADA CUPON
	"Nemo"						NVARCHAR(100),
	"FechaIni"					DATE,
	"FechaFin"					DATE,
	"CodTipoMod"				NVARCHAR(1),
	"Plazo"						BIGINT,
	-- DATOS SALIDA CUPON
	"OUT_GrupoValorizacion"		BIGINT,	 
	"OUT_FechaSolicitud" 		SECONDDATE,
 	"OUT_IdEstado" 				NVARCHAR(3),
	--
	"OUT_Nemo"					NVARCHAR(100),				-- E|S 	- Nemotécnico
	"OUT_FechaIni"				DATE,						-- E|S 	- Pendiente documento definicion
	"OUT_FechaFin"				DATE,						-- E|S 	- Pendiente documento definicion
	"OUT_CodTipoMod"			NVARCHAR(1),				-- E| 	- Pendiente documento definicion
	"OUT_Plazo"					BIGINT,						-- E|S 	- Pendiente documento definicion		
	"OUT_Glosa"					NVARCHAR(100),				--  |S	- Pendiente documento definicion		
	"OUT_CodigoEmisor"			NVARCHAR(40),				--  |S 	- Pendiente documento definicion
	"OUT_FechaEmision"			DATE,						--  |S 	- Pendiente documento definicion
	"OUT_FechaVencimiento"		DATE,						--  |S 	- Pendiente documento definicion
	"OUT_TipoMod"				NVARCHAR(40),				--  |S 	- Pendiente documento definicion
	"OUT_TipoUnidad"			NVARCHAR(40),				--  |S 	- Pendiente documento definicion -- equivale a tipo moneda en valoriz.
	"OUT_ValorReferencial"		NVARCHAR(100),				--  |S 	- Pendiente documento definicion
	"OUT_CuponAmortizacion"		DECIMAL(26,8),				--  |S 	- Pendiente documento definicion
	"OUT_CuponFactorAjuste"		DECIMAL(26,8),				--  |S 	- Pendiente documento definicion
	"OUT_CuponFechaEfectiva"	DATE,						--  |S 	- Pendiente documento definicion
	"OUT_CuponFechaHabil"		DATE,						--  |S 	- Pendiente documento definicion
	"OUT_CuponFlujo"			DECIMAL(26,8),				--  |S 	- Pendiente documento definicion
	"OUT_CuponIntereses"		DECIMAL(26,8),				--  |S 	- Pendiente documento definicion
	"OUT_CuponSecuencia"		BIGINT,						--  |S 	- Pendiente documento definicion
	-- salida invocacion webservice desde el job
	"OUT_WsCallerCodSalida"		NVARCHAR(100),				--Salida caller java consulta WS
	"OUT_WsCallerDescSalida"	NVARCHAR(100),				--Salida caller java consulta WS		
	PRIMARY KEY ("IdProcesamientoCupon")
);

set schema bscl;
-- SP: "BSCL.Custodia.BD.Servicios::VAL.ValorizarCustodia"
create column table "BSCL.Plataforma.BD::VAL.InstrumentosPQValorizaCustodia"(
	"IdInterno"							BIGINT,
	"IdValorizacion"					BIGINT,
	"FechaValorizacion"					NVARCHAR(10),
	"ValorizaACompra"					NVARCHAR(1),
	"ValorizaAMercado"					NVARCHAR(1),
	"IdClasificacionTipoInstrumento" 	NVARCHAR(3),
	"IdTipoInstrumento"					NVARCHAR(3),
	"IdInstrumento"						BIGINT,
	"Nemotecnico"						NVARCHAR(40),
	"IdMoneda"							NVARCHAR(3),
	"Cantidad"							DECIMAL(26,8),
	"Precio"							DECIMAL(26,8),
	"Tamano"							BIGINT,
	"UsoPrecio"							NVARCHAR(40),
	"TipoPrecio"						NVARCHAR(40),
	"Fuente"							NVARCHAR(3),
	"EsCuentaPropia"					NVARCHAR(1),
	"FechaVencimientoOperacion" 		DATE,
	"Tasa"								DECIMAL(26,8),
	"Plazo"								BIGINT,
	"PlazoResidual"						BIGINT,
	"Base"								BIGINT,
	--
	"TicketProcesamiento"				BIGINT,
	PRIMARY KEY ("IdInterno")	
);

create column table "BSCL.Plataforma.BD::VAL.InstrumentosWSValorizaCustodia"(
	"IdInterno"							BIGINT,
	"IdValorizacion"					BIGINT,
	"FechaValorizacion"					NVARCHAR(10), 
	"ValorizaACompra"					NVARCHAR(1),
	"ValorizaAMercado"					NVARCHAR(1),
	"ValorizaPor"						NVARCHAR(1),
	"IdClasificacionTipoInstrumento" 	NVARCHAR(3),
	"IdTipoInstrumento"					NVARCHAR(3),
	"IdInstrumento"						BIGINT,
	"Nemotecnico"						NVARCHAR(40),
	"IdEmisor"							NVARCHAR(40),
	"FechaEmision"						NVARCHAR(10),
	"FechaVencimiento"					NVARCHAR(10),
	"IdTipoReajuste"					NVARCHAR(3),
	"Cantidad"							DECIMAL(26,8),
	"ValorRescate" 						DECIMAL(26,8),
	"PrecioCompra"						DECIMAL(26,8), 
	"TasaCompra"						DECIMAL(26,8),
	"PrecioSubyacente"					DECIMAL(26,8),
	"TasaSubyacente"					DECIMAL(26,8),
	"PrecioGarantia"					DECIMAL(26,8),
	"TasaGarantia"						DECIMAL(26,8),
	"TasaTIF"							DECIMAL(26,8),
	"UsoPrecio"							NVARCHAR(40),
	"TipoPrecio"						NVARCHAR(40),
	"Fuente"							NVARCHAR(40),
	"EsCuentaPropia"					NVARCHAR(1),
	"IndicadorUsoTasa"					NVARCHAR(2),
	"TipoValorizacion"					NVARCHAR(10),
	--
	"TicketProcesamiento"				BIGINT,
	PRIMARY KEY ("IdInterno")	
);

create column table "BSCL.Plataforma.BD::VAL.ProcesamientoPQValorizaCustodia"(
	"IdProcesamiento"						BIGINT,
	"DimTasa" 								nvarchar(4),
	"DimTiempo" 							nvarchar(2),
	"FechaCalculada"						NVARCHAR(10),
	"PrecioValorizar"						DECIMAL(26,8),
	"Valorizado"							NVARCHAR(1), -- Indica si fue valorizado
	--
	"IdInterno"								BIGINT, -- Referencia con tabla InstrumentosPQValorizaCustodia
	"IdValorizacion"						BIGINT,
	"FechaValorizacion"						NVARCHAR(10),
	"ValorizaACompra"						NVARCHAR(1),
	"ValorizaAMercado"						NVARCHAR(1),
	"IdClasificacionTipoInstrumento" 		NVARCHAR(3),
	"IdTipoInstrumento"						NVARCHAR(3),
	"IdInstrumento"							BIGINT,
	"Nemotecnico"							NVARCHAR(40),
	"IdMoneda"								NVARCHAR(3),
	"Cantidad"								DECIMAL(26,8),
	"Precio"								DECIMAL(26,8),
	"Tamano"								BIGINT, 
	"UsoPrecio"								NVARCHAR(40),
	"TipoPrecio"							NVARCHAR(40),
	"Fuente"								NVARCHAR(3),
	"EsCuentaPropia"						NVARCHAR(1),
	"FechaVencimientoOperacion" 			DATE,
	"Tasa"									DECIMAL(26,8),
	"Plazo"									BIGINT,
	"PlazoResidual"							BIGINT,
	"Base"									BIGINT,	
	--salida consulta precio tasa instrumento		
	"PTI_IdListaInstrumentos"				BIGINT,
	"PTI_CodigoMensaje"						NVARCHAR(100),
	"PTI_Fecha"								DATE,
	"PTI_Precio"							DECIMAL(26,8),
	"PTI_Tasa"								DECIMAL(26,8),
	"PTI_IdMoneda"							NVARCHAR(3),
	"PTI_UsoPrecioTasa"						NVARCHAR(40),
	"PTI_TipoPrecioTasa"					NVARCHAR(40),
	"PTI_IndicadorUsoTasa"					NVARCHAR(2),
	"PTI_EsCuentaPropia"					NVARCHAR(1),
	"PTI_Fuente"							NVARCHAR(40),
	"PTI_TablaFuente"						NVARCHAR(60),
	"PTI_UltimoPrecioTransado"				DECIMAL(26,8),
	"PTI_VariacionDiaria"					DECIMAL(26,8),
	"PTI_VolumenTransado"					DECIMAL(26,8),
	"PTI_MontoTransado"						DECIMAL(26,8),
	"PTI_MejorPrecioCompra"					DECIMAL(26,8),
	"PTI_MejorPrecioVenta"					DECIMAL(26,8),
	"PTI_Tamano"							BIGINT,
	"PTI_PrecioBase"						DECIMAL(26,8),
	"PTI_PrecioPromedioCompra"				DECIMAL(26,8),
	"PTI_PrecioPromedioVenta"				DECIMAL(26,8),
	"PTI_DescripcionMensaje"				NVARCHAR(5000),
	"PTI_CodigoDato1"						NVARCHAR(100),
	"PTI_ValorDato1"						NVARCHAR(520),
	"PTI_CodigoDato2"						NVARCHAR(100),
	"PTI_ValorDato2"						NVARCHAR(520),
	--salida valorizacion
	"VAL_IdValorizacion"					BIGINT,
	"VAL_CodigoMensaje"						NVARCHAR(100),
	"VAL_ValorizaACompra"					NVARCHAR(1),
	"VAL_ValorizaAMercado"					NVARCHAR(1),
	"VAL_FechaValorizacion"					DATE,
	"VAL_IdClasificacionTipoInstrumento"	NVARCHAR(3),
	"VAL_IdTipoInstrumento"					NVARCHAR(3),
	"VAL_IdInstrumento"						BIGINT,
	"VAL_Nemotecnico"						NVARCHAR(40),
	"VAL_IdMoneda"							NVARCHAR(3),
	"VAL_Cantidad"							DECIMAL(26,8),
	"VAL_Precio"							DECIMAL(26,8),
	"VAL_Tamano"							BIGINT,
	"VAL_UsoPrecio"							NVARCHAR(40),
	"VAL_TipoPrecio"						NVARCHAR(40),
	"VAL_Fuente"							NVARCHAR(40),
	"VAL_EsCuentaPropia"					NVARCHAR(1),
	"VAL_Monto"								DECIMAL(26,8),
	"VAL_MontoMercado"						DECIMAL(26,8),
	"VAL_MontoMonedaLocal"					DECIMAL(26,8),
	"VAL_MontoMonedaLocalMercado"			DECIMAL(26,8),
	"VAL_FechaPrecio"						DATE,
	"VAL_PrecioMercado"						DECIMAL(26,8),
	"VAL_IdMonedaMercado"					NVARCHAR(3),
	"VAL_TablaFuente"						NVARCHAR(60),
	"VAL_TamanoMercado"						BIGINT,
	"VAL_DescripcionMensaje"				NVARCHAR(5000),
	"VAL_CodigoDato1"						NVARCHAR(100),
	"VAL_ValorDato1"						NVARCHAR(520),
	"VAL_CodigoDato2"						NVARCHAR(100),
	"VAL_ValorDato2"						NVARCHAR(520),
	"INT_IdProceso"							BIGINT,
	"INT_CodigoMensaje"						NVARCHAR(100),
	"INT_Cantidad"							DECIMAL(26,8),
	"INT_Precio"							DECIMAL(26,8),
	"INT_Tasa"								DECIMAL(26,8),
	"INT_Plazo"								BIGINT,
	"INT_PlazoResidual"						BIGINT,
	"INT_Base"								BIGINT,
	"INT_Monto"								DECIMAL(26,8),
	"INT_MontoMonedaLocal"					DECIMAL(26,8),
	"INT_DiasTranscurridos"					BIGINT,
	"INT_TasaPorPlazo"						DECIMAL(26,8),
	"INT_TasaPorDiasTranscurridos" 			DECIMAL(26,8),
	"INT_InteresDiario"						DECIMAL(26,8),
	"INT_InteresAcumulado"					DECIMAL(26,8),
	"INT_ValorPresente"						DECIMAL(26,8),
	"INT_MontoAPlazo"						DECIMAL(26,8),
	"INT_MontoAPlazoMonedaLocal" 			DECIMAL(26,8),
	"INT_DescripcionMensaje" 				NVARCHAR(5000),
	"INT_CodigoDato1"						NVARCHAR(100),
	"INT_ValorDato1"						NVARCHAR(520),
	"INT_CodigoDato2"						NVARCHAR(100),
	"INT_ValorDato2"						NVARCHAR(520),
	--
	"TicketProcesamiento"				BIGINT,
	PRIMARY KEY ("IdProcesamiento")	
);

create column table "BSCL.Plataforma.BD::VAL.ProcesamientoWSValorizaCustodia"(
	"IdProcesamiento"						BIGINT,
	"DimTasa" 								nvarchar(4),
	"DimTiempo" 							nvarchar(2),
	"FechaCalculada"						NVARCHAR(10),
	"ValorRescateValorizar"					DECIMAL(26,8),
	"PrecioValorizar"						DECIMAL(26,8),
	"TasaValorizar"							DECIMAL(26,8),
	"TipoPrecioTasa"						NVARCHAR(40), 	-- Compra, Mercado, TIF, Corredora, Subyacente, Garantia (seteado en matriz)
	"Valorizado"							NVARCHAR(1), 	-- Indica si fue valorizado
	--	
	"IdInterno"								BIGINT,	-- Referencia con tabla InstrumentosWSValorizaCustodia
	"IdValorizacion"						BIGINT,
	"FechaValorizacion"						NVARCHAR(10), 
	"ValorizaACompra"						NVARCHAR(1),
	"ValorizaAMercado"						NVARCHAR(1),
	"ValorizaPor"							NVARCHAR(1),
	"IdClasificacionTipoInstrumento" 		NVARCHAR(3),
	"IdTipoInstrumento"						NVARCHAR(3),
	"IdInstrumento"							BIGINT,
	"Nemotecnico"							NVARCHAR(40),
	"IdEmisor"								NVARCHAR(40),
	"FechaEmision"							NVARCHAR(10),
	"FechaVencimiento"						NVARCHAR(10),
	"IdTipoReajuste"						NVARCHAR(3),
	"Cantidad"								DECIMAL(26,8),
	"ValorRescate" 							DECIMAL(26,8),
	"PrecioCompra"							DECIMAL(26,8),
	"TasaCompra"							DECIMAL(26,8),
	"PrecioSubyacente"						DECIMAL(26,8),
	"TasaSubyacente"						DECIMAL(26,8),
	"PrecioGarantia"						DECIMAL(26,8),
	"TasaGarantia"							DECIMAL(26,8),
	"TasaTIF"								DECIMAL(26,8),
	"UsoPrecio"								NVARCHAR(40),
	"TipoPrecio"							NVARCHAR(40),
	"Fuente"								NVARCHAR(40), 
	"EsCuentaPropia"						NVARCHAR(1),
	"IndicadorUsoTasa"						NVARCHAR(2),
	"TipoValorizacion"						NVARCHAR(10),
	"PTI_IdListaInstrumentos"				BIGINT,
	"PTI_CodigoMensaje"						NVARCHAR(100),
	"PTI_Fecha"								DATE,
	"PTI_Precio"							DECIMAL(26,8),
	"PTI_Tasa"								DECIMAL(26,8),
	"PTI_IdMoneda"							NVARCHAR(3),
	"PTI_UsoPrecioTasa"						NVARCHAR(40),
	"PTI_TipoPrecioTasa"					NVARCHAR(40),
	"PTI_IndicadorUsoTasa"					NVARCHAR(2),
	"PTI_EsCuentaPropia"					NVARCHAR(1),
	"PTI_Fuente"							NVARCHAR(40),
	"PTI_TablaFuente"						NVARCHAR(60),
	"PTI_UltimoPrecioTransado"				DECIMAL(26,8),
	"PTI_VariacionDiaria"					DECIMAL(26,8),
	"PTI_VolumenTransado"					DECIMAL(26,8),
	"PTI_MontoTransado"						DECIMAL(26,8),
	"PTI_MejorPrecioCompra"					DECIMAL(26,8),
	"PTI_MejorPrecioVenta"					DECIMAL(26,8),
	"PTI_Tamano"							BIGINT,
	"PTI_PrecioBase"						DECIMAL(26,8),
	"PTI_PrecioPromedioCompra"				DECIMAL(26,8),
	"PTI_PrecioPromedioVenta"				DECIMAL(26,8),
	"PTI_DescripcionMensaje"				NVARCHAR(5000),
	"PTI_CodigoDato1"						NVARCHAR(100),
	"PTI_ValorDato1"						NVARCHAR(520),
	"PTI_CodigoDato2"						NVARCHAR(100),
	"PTI_ValorDato2"						NVARCHAR(520),
	"VAL_IdValorizacion"					BIGINT,
	"VAL_CodigoMensaje"						NVARCHAR(100),
	"VAL_ValorizaACompra"					NVARCHAR(1),
	"VAL_ValorizaAMercado"					NVARCHAR(1),
	"VAL_FechaValorizacion"					DATE,
	"VAL_IdClasificacionTipoInstrumento"	NVARCHAR(3),
	"VAL_IdTipoInstrumento"					NVARCHAR(3),
	"VAL_IdInstrumento"						BIGINT,
	"VAL_Nemotecnico"						NVARCHAR(40),
	"VAL_IdTipoReajuste"					NVARCHAR(3),
	"VAL_Cantidad"							DECIMAL(26,8),
	"VAL_Precio"							DECIMAL(26,8),
	"VAL_UsoPrecio"							NVARCHAR(40),
	"VAL_TipoPrecio"						NVARCHAR(40),
	"VAL_Fuente"							NVARCHAR(3),
	"VAL_EsCuentaPropia"					NVARCHAR(1),	
	"VAL_Tasa"								DECIMAL(26,8),
	"VAL_Plazo"								BIGINT,
	"VAL_FechaVencimientoOperacion"			DATE,
	"VAL_Base"								BIGINT,
	"VAL_PlazoResidual"						BIGINT,		
	"VAL_Monto"								DECIMAL(26,8),
	"VAL_MontoMercado"						DECIMAL(26,8),
	"VAL_MontoMonedaLocal"					DECIMAL(26,8),
	"VAL_MontoMonedaLocalMercado"			DECIMAL(26,8),	
	"VAL_DiasTranscurridos"					BIGINT,
	"VAL_TasaPorPlazo"						DECIMAL(26,8),
	"VAL_TasaPorDiasTranscurridos"			DECIMAL(26,8),
	"VAL_InteresDiario"						DECIMAL(26,8),
	"VAL_InteresAcumulado"					DECIMAL(26,8),
	"VAL_ValorPresente"						DECIMAL(26,8),
	"VAL_MontoAPlazo"						DECIMAL(26,8),
	"VAL_MontoAPlazoMonedaLocal" 			DECIMAL(26,8),
	"VAL_FechaPrecio"						DATE,
	"VAL_PrecioMercado"						DECIMAL(26,8),
	"VAL_IdMonedaMercado"					NVARCHAR(3),
	"VAL_TablaFuente"						NVARCHAR(60),
	"VAL_TamanoMercado"						BIGINT,	
	"VAL_Duration"							DECIMAL(26,8),
	"VAL_Convexidad"						DECIMAL(26,8),
	"VAL_PlazoValorizador"					DECIMAL(26,8),	
	"VAL_NumeroCupon"						BIGINT,
	"VAL_MontoCupon"						DECIMAL(26,8),		
	"VAL_DescripcionMensaje"				NVARCHAR(5000),
	"VAL_CodigoDato1"						NVARCHAR(100),
	"VAL_ValorDato1"						NVARCHAR(520),
	"VAL_CodigoDato2"						NVARCHAR(100),
	"VAL_ValorDato2"						NVARCHAR(520),
	"INT_IdProceso"							BIGINT,
	"INT_CodigoMensaje"						NVARCHAR(100),
	"INT_Cantidad"							DECIMAL(26,8),
	"INT_Precio"							DECIMAL(26,8),
	"INT_Tasa"								DECIMAL(26,8),
	"INT_Plazo"								BIGINT,
	"INT_PlazoResidual"						BIGINT,
	"INT_Base"								BIGINT,
	"INT_Monto"								DECIMAL(26,8),
	"INT_MontoMonedaLocal"					DECIMAL(26,8),
	"INT_DiasTranscurridos"					BIGINT,
	"INT_TasaPorPlazo"						DECIMAL(26,8),
	"INT_TasaPorDiasTranscurridos" 			DECIMAL(26,8),
	"INT_InteresDiario"						DECIMAL(26,8),
	"INT_InteresAcumulado"					DECIMAL(26,8),
	"INT_ValorPresente"						DECIMAL(26,8),
	"INT_MontoAPlazo"						DECIMAL(26,8),
	"INT_MontoAPlazoMonedaLocal" 			DECIMAL(26,8),
	"INT_DescripcionMensaje" 				NVARCHAR(5000),
	"INT_CodigoDato1"						NVARCHAR(100),
	"INT_ValorDato1"						NVARCHAR(520),
	"INT_CodigoDato2"						NVARCHAR(100),
	"INT_ValorDato2"						NVARCHAR(520),
	"CUP_IdTarea" 							BIGINT,
	"CUP_GrupoValorizacion"					BIGINT,	 
	"CUP_FechaSolicitud" 					SECONDDATE,
 	"CUP_IdEstado" 							NVARCHAR(3),
	"CUP_Nemo"								NVARCHAR(100),
	"CUP_FechaIni"							DATE,		
	"CUP_FechaFin"							DATE,
	"CUP_CodTipoMod"						NVARCHAR(1),
	"CUP_Plazo"								BIGINT,		
	"CUP_Glosa"								NVARCHAR(100),		
	"CUP_CodigoEmisor"						NVARCHAR(40),
	"CUP_FechaEmision"						DATE,
	"CUP_FechaVencimiento"					DATE,
	"CUP_TipoMod"							NVARCHAR(40),
	"CUP_TipoUnidad"						NVARCHAR(40),
	"CUP_ValorReferencial"					NVARCHAR(100),
	"CUP_CuponAmortizacion"					DECIMAL(26,8),
	"CUP_CuponFactorAjuste"					DECIMAL(26,8),
	"CUP_CuponFechaEfectiva"				DATE,
	"CUP_CuponFechaHabil"					DATE,
	"CUP_CuponFlujo"						DECIMAL(26,8),
	"CUP_CuponIntereses"					DECIMAL(26,8),
	"CUP_CuponSecuencia"					BIGINT,
	"CUP_WsCallerCodSalida"					NVARCHAR(100),
	"CUP_WsCallerDescSalida"				NVARCHAR(100),
	--
	"TicketProcesamiento"				BIGINT,
	PRIMARY KEY ("IdProcesamiento")	
);


--###################################
-- SECUENCIAS
--###################################
-- SP: "BSCL.Custodia.BD.Servicios::VAL.Cupon"
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::IdProcesamientoCupon" 
START WITH 1 INCREMENT BY 1 MINVALUE  1;
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::TicketProcesamientoCupon" 
START WITH 1 INCREMENT BY 1 MINVALUE  1;

-- SP: "BSCL.Custodia.BD.Servicios::VAL.ValorizarCustodia"
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::IdInstrumentoValorizaCustodia" 
START WITH 1 INCREMENT BY 1 MINVALUE  1;
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodia" 
START WITH 1 INCREMENT BY 1 MINVALUE  1;
CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::TicketProcesamientoValorizaCustodia" 
START WITH 1 INCREMENT BY 1 MINVALUE  1;

CREATE SEQUENCE  "BSCL.Plataforma.BD.Secuencias::IdProcesamientoValorizaCustodiaMasiva"
START WITH 1 INCREMENT BY 1 MINVALUE  1;










CREATE TYPE "BSCL.Plataforma.BD::VAL.InstrumentosValorizadosRV" AS TABLE  ( 
	"IdValorizacion"					BIGINT,
	"CodigoMensaje"						NVARCHAR(100),
	"ValorizaACompra"					NVARCHAR(1),
	"ValorizaAMercado"					NVARCHAR(1),
	"FechaValorizacion"					DATE,
	"IdClasificacionTipoInstrumento"	NVARCHAR(3),
	"IdTipoInstrumento"					NVARCHAR(3),
	"IdInstrumento"						BIGINT,
	"Nemotecnico"						NVARCHAR(40),
	"IdMoneda"							NVARCHAR(3),
	"Cantidad"							DECIMAL(26,8),
	"Precio"							DECIMAL(26,8),
	"Tamano"							BIGINT,
	--PARA OBTENCION DE PRECIOS O TASAS
	"UsoPrecio"							NVARCHAR(40),
	"TipoPrecio"						NVARCHAR(40),
	"Fuente"							NVARCHAR(40),
	"EsCuentaPropia"					NVARCHAR(1),
	--MONTOS VALORIZADOS
	"Monto"								DECIMAL(26,8),
	"MontoMercado"						DECIMAL(26,8),
	"MontoMonedaLocal"					DECIMAL(26,8),
	"MontoMonedaLocalMercado"			DECIMAL(26,8),
	--DE OBTENCION DE PRECIOS O TASAS
	"FechaPrecio"						DATE,
	"PrecioMercado"						DECIMAL(26,8),
	"IdMonedaMercado"					NVARCHAR(3),
	"TablaFuente"						NVARCHAR(60),
	"TamanoMercado"						BIGINT,
	--ERRORES O ADVERTENCIAS
	"DescripcionMensaje"				NVARCHAR(5000),
	"CodigoDato1"						NVARCHAR(100),
	"ValorDato1"						NVARCHAR(520),
	"CodigoDato2"						NVARCHAR(100),
	"ValorDato2"						NVARCHAR(520)
);
	
CREATE TYPE "BSCL.Plataforma.BD::VAL.InstrumentosValorizadosRF" AS TABLE  ( 
	"IdValorizacion"					BIGINT,
	"CodigoMensaje"						NVARCHAR(100),
	"ValorizaACompra"					NVARCHAR(1),
	"ValorizaAMercado"					NVARCHAR(1),
	"FechaValorizacion"					DATE,
	"IdClasificacionTipoInstrumento"	NVARCHAR(3),
	"IdTipoInstrumento"					NVARCHAR(3),
	"IdInstrumento"						BIGINT,
	"Nemotecnico"						NVARCHAR(40),
	"IdTipoReajuste"					NVARCHAR(3), -- reemplaza a idMoneda
	"Cantidad"							DECIMAL(26,8),
	"Precio"							DECIMAL(26,8),
	--"Tamano"							BIGINT,
	--PARA OBTENCION DE PRECIOS O TASAS
	"UsoPrecio"							NVARCHAR(40),
	"TipoPrecio"						NVARCHAR(40),
	"Fuente"							NVARCHAR(3),
	"EsCuentaPropia"					NVARCHAR(1),
	--PARA OBTENCION DE INTERESES
	"Tasa"								DECIMAL(26,8),
	"Plazo"								BIGINT, -- PLAZO PARA LA OBENCION DE INTERESES
	"FechaVencimientoOperacion"			DATE,
	"Base"								BIGINT,
	"PlazoResidual"						BIGINT,
	--MONTOS VALORIZADOS
	"Monto"								DECIMAL(26,8),
	"MontoMercado"						DECIMAL(26,8),
	"MontoMonedaLocal"					DECIMAL(26,8),
	"MontoMonedaLocalMercado"			DECIMAL(26,8),
	--DE CALCULO DE INTERESES
	"DiasTranscurridos"					BIGINT,
    "TasaPorPlazo"						DECIMAL(26,8),
    "TasaPorDiasTranscurridos"			DECIMAL(26,8),
    "InteresDiario"						DECIMAL(26,8),
    "InteresAcumulado"					DECIMAL(26,8),
    "ValorPresente"						DECIMAL(26,8),
    "MontoAPlazo"						DECIMAL(26,8),
    "MontoAPlazoMonedaLocal" 			DECIMAL(26,8),
	--DE OBTENCION DE PRECIOS O TASAS
	"FechaPrecio"						DATE,
	"PrecioMercado"						DECIMAL(26,8),
	"IdMonedaMercado"					NVARCHAR(3),
	"TablaFuente"						NVARCHAR(60),
	"TamanoMercado"						BIGINT,
	--DEL VALORIZADOR
	"Duration"							DECIMAL(26,8),
	"Convexidad"						DECIMAL(26,8),
	"PlazoValorizador"					DECIMAL(26,8),
	--CORTE DE CUPON
	"NumeroCupon"						BIGINT,
	"MontoCupon"						DECIMAL(26,8),
	--ERRORES O ADVERTENCIAS
	"DescripcionMensaje"				NVARCHAR(5000),
	"CodigoDato1"						NVARCHAR(100),
	"ValorDato1"						NVARCHAR(520),
	"CodigoDato2"						NVARCHAR(100),
	"ValorDato2"						NVARCHAR(520)
);
	

CREATE TYPE "BSCL.Plataforma.BD::VAL.InteresesCalculados" AS TABLE  ( 
	"IdProceso"			BIGINT,
	"CodigoMensaje"		NVARCHAR(100),
	"Cantidad"			DECIMAL(26,8),
	"Precio"			DECIMAL(26,8),
	"Tasa"				DECIMAL(26,8),
	"Plazo"				BIGINT,
	"PlazoResidual"		BIGINT,
    "Base"				BIGINT,
    ---------DEVUELTOS
    "Monto"				DECIMAL(26,8),
    "MontoMonedaLocal"	DECIMAL(26,8),
    "DiasTranscurridos"	BIGINT,
    "TasaPorPlazo"		DECIMAL(26,8),
    "TasaPorDiasTranscurridos" DECIMAL(26,8),
    "InteresDiario"		DECIMAL(26,8),
    "InteresAcumulado"	DECIMAL(26,8),
    "ValorPresente"		DECIMAL(26,8),
    "MontoAPlazo"		DECIMAL(26,8),
    "MontoAPlazoMonedaLocal" DECIMAL(26,8),
    
    "DescripcionMensaje" NVARCHAR(5000),
	"CodigoDato1"		NVARCHAR(100),
	"ValorDato1"		NVARCHAR(520),
	"CodigoDato2"		NVARCHAR(100),
	"ValorDato2"		NVARCHAR(520)
);


CREATE SEQUENCE "BSCL.Plataforma.BD.Secuencias::VAL.Valorizar" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;
	   


commit;
