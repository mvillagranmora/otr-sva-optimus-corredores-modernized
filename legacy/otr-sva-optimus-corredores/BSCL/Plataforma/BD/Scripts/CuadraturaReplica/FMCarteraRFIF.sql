select * from "2022_05_31_FMCARTERARFIF" f where FECHA  = '2022-05-31'  and TIPOCOMPROM  = 'CT'  and NOMBRELISTADO  = 'CPLVPC72  '
and RUTCLIENTE  = ' 60805000-0' AND emisor = 'BCI' and NEMOTECNICO  = 'PAGARE NR'

336	6297309959.0000
339	XXX2994773.0000
338	351617676.0000
338	2252246288.0000

select * from "BSCL.Replica.BD.Vistas::ReplicaFMCarteraRFIF" brbvrfr where TIPOCOMPROM  = 'VP'
and RUTCLIENTE  = ' 60805000-0' AND emisor = 'BCI' and NEMOTECNICO  = 'PAGARE NR'  and nemotecnico != 'PDBC'

338	351617676
338	2252246288
336	6297309959

select * from "2022_05_31_FMCARTERARFIF" f where RUTCLIENTE  = 

select sebra.c1 - optimus.c1,* FROM 
(
select sum(nominal) as c1,rutcliente,nemotecnico,emisor,TIPOCOMPROM,tiporeajpapel
from "2022_05_31_FMCARTERARFIF" brbvrfr 
where fecha = '2022-05-31'  and NOMBRELISTADO  = 'CPLVPC72  '
and RUTCLIENTE = ' 73923400-K' and NEMOTECNICO  = 'PAGARE R' AND EMISOR = 'BICE'
group by nemotecnico,rutcliente,emisor,TIPOCOMPROM,tiporeajpapel
) as sebra
left join
(
select sum(nominal) as c1,nemotecnico,rutcliente,emisor,TIPOCOMPROM,tiporeajpapel
from "BSCL.Replica.BD.Vistas::ReplicaFMCarteraRFIF" brbvrfr 
where RUTCLIENTE = ' 73923400-K' and NEMOTECNICO  = 'PAGARE R' AND EMISOR = 'BICE'
group by nemotecnico,rutcliente,emisor,TIPOCOMPROM,tiporeajpapel
) as optimus on sebra.nemotecnico = optimus.nemotecnico and sebra.rutcliente = optimus.rutcliente
and sebra.emisor = optimus.emisor and sebra.tiporeajpapel = optimus.tiporeajpapel
and sebra.tipocomprom = optimus.tipocomprom
where sebra.c1 != optimus.c1 or optimus.c1 is null
order by sebra.rutcliente,sebra.nemotecnico,sebra.emisor


SELECT TIPOCOMPROM ,NOMINAL as n ,TIPOREAJPAPEL ,* 
from "BSCL.Replica.BD.Vistas::ReplicaFMCarteraRFIF" brbvrfr
--where RUTCLIENTE = ' 60706000-2' and NEMOTECNICO  = 'PAGARE NR' AND EMISOR = 'ITAUCORP' se abren carteras 
--where RUTCLIENTE = ' 60706000-2' and NEMOTECNICO  = 'PAGARE NR' AND EMISOR = 'BCI' -- se abren carteras
--where RUTCLIENTE = ' 60706000-2' and NEMOTECNICO  = 'PDBC' AND EMISOR = 'CENTRAL'
where RUTCLIENTE = ' 73923400-K' and NEMOTECNICO  = 'PAGARE R' AND EMISOR = 'BICE' -- se abren carteras
--where RUTCLIENTE = ' 60805000-0' and NEMOTECNICO  = 'PDBC' AND EMISOR = 'CENTRAL' and tipocomprom != 'CT'-- se abren carteras
order by n

6550000000.0000
8350000000.0000
9700000000.0000
13000000000.0000
15000000000.0000
17000000000.0000
20070000000.0000
29930000000.0000
33805000000.0000

SELECT TIPOCOMPROM ,NOMINAL as n,TIPOREAJPAPEL ,* 
from "2022_05_31_FMCARTERARFIF" brbvrfr
--where RUTCLIENTE = ' 60706000-2' and NEMOTECNICO  = 'PAGARE NR' AND EMISOR = 'ITAUCORP'
--where RUTCLIENTE = ' 60805000-0' and NEMOTECNICO  = 'PDBC' AND EMISOR = 'CENTRAL' and tipocomprom != 'CT' -- se abren carteras
where RUTCLIENTE = ' 73923400-K' and NEMOTECNICO  = 'PAGARE R' AND EMISOR = 'BICE' -- se abren carteras
--where RUTCLIENTE = ' 60706000-2' and NEMOTECNICO  = 'PDBC' AND EMISOR = 'CENTRAL'
and FECHA  = '2022-05-31' and NOMBRELISTADO  = 'CPLVPC72  '
order by n

Select 
		 			'PACTO IF' as tipo,
		            CAST(fecPro."Valor" AS VARCHAR(10))  AS "FECHA", -- CHAR(10)
		            Det.Sec_Comprom  AS "SEC_CAR_INVER", -- DECIMAL
		            Det.Sec_Car_Corr  AS "SEC_CAR_CORR", -- DECIMAL
		            Det.Sec_Car_Cli  AS "SEC_CAR_CLI", -- VARCHAR(100)
		            Det.Lin_Comprom   AS "LIM_COMPROM", -- DECIMAL
		            0  AS "INTERMEDIADO", -- INT
		            DAYS_BETWEEN(fecPro."Valor",FEC_VCTO) AS "DIASVCTO",--7      AS "DIASVCTO", -- DECIMAL
		           (Det.Val_resc*Det.Cantidad) AS "NOMINAL",--8    
				   Monto_Ini_Cap*Det.Cantidad AS "MTOCOMPRAMONPAPEL", --9 --23-02-2011    
		            COALESCE (0 , 0 )  AS "TIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "INTDIATIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "REAJDIATIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "INTACUMTIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "REAJACUMTIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOVPTIRCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOVPTIRCOMPRAMAN", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "TIRMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOVPTIRMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MENVALORMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MAYVALORMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "TASAMCDOBCS", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MTOVPTIRMCDOBCS", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MENVALORMCDOBCS", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "MAYVALORMCDOBCS", -- DECIMAL
		            COALESCE (CAST('' AS VARCHAR(10)),'')  AS "FECPROXCUPON", -- CHAR(10)
		            COALESCE ( 0 , 0 )  AS "VALPROXCUPON", -- DECIMAL
		            COALESCE ( Det.Monto_Comprom*Det.Cantidad , 0 )  AS "MTOVALORVCTO", -- DECIMAL
		            COALESCE (  (Det.Cant_Pc_Comprom*Det.Cantidad) , 0 )  AS "MTOMONVALORVCTO", -- DECIMAL
		            (Monto_Ini_Cap*Det.Cantidad)   AS "MTOCOMPRAMONPESOS", -- DECIMAL          
		            COALESCE (0 , 0 )  AS "NUM_CUPON", -- INT
		            COALESCE ( 0 , 0 )  AS "FRECUENCIA_CUPON", -- INT
		            COALESCE ( 0 , 0 )  AS "TASATERA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "INTERESDEVENGADO", -- DECIMAL
		            COALESCE (CAST('' AS VARCHAR(10)),'')  AS "FECHATASABCS", -- CHAR(10)
		            COALESCE ( 0 , 0 )  AS "AMORTCUPON", -- DECIMAL
		            COALESCE (0, 0 )  AS "INTECUPON", -- DECIMAL
		            COALESCE (CAST('' AS VARCHAR(4)),'')  AS "TIPOPRODUCTO", -- CHAR(4)
		           '2022-08-222022-08-23XXX2022-08-22 18:57:20.580'  AS "LLAVEREPORTE", -- CHAR(50)
		            'SYSTEM'  AS "USUARIOLISTADO", -- CHAR(15)
		           'CPLVPC72  '  AS "NOMBRELISTADO", -- CHAR(10)
		            CAST(fecPro."Valor" AS VARCHAR(10))   AS "FECHACREACIONLST", -- DATETIME
		            '00:00:00.00'  AS "HORACREACIONLST", -- CHAR(11)
		            CAST(fecPro."Valor" AS VARCHAR(7))   AS "CODPERLST", -- CHAR(7)
		            CAST(fecPro."Valor" AS VARCHAR(10))  AS "FECHAANTERIORLST", -- CHAR(10)
		            CAST(fecPro."Valor" AS VARCHAR(10))  AS "FECHAVALORIZACIONLST", -- CHAR(10)
		            'C'  AS "CODTASALST", -- CHAR(2)
		            ''  AS "CODREAJLST", -- CHAR(3)
		            'PT'  AS "TIPOCARTERALST", -- CHAR(2)
		            0  AS "TIPOAGRUPLST", -- INT
		            Det.Sec_Car_Corr  AS "NUMOPERACION", -- DECIMAL
		            Cab.Fec_Operac  AS "FECHAINVERSION", -- CHAR(10)
		            Cab.Fec_Operac  AS "FECHAINICIOPACTO", -- CHAR(10)
		            Cab.Rut_Cli  AS "RUTCLIENTE", -- CHAR(11)
		            Cab.Sec_Rut_Cli  AS "CODCLIENTE", -- DECIMAL
		            ''  AS "NOMBRECLIENTE", -- VARCHAR(130)
		            ''  AS "CUENTACLIENTE", -- VARCHAR(100)
		            Det.Tipo_Fam  AS "TIPOFAMILIA", -- CHAR(3)
		             Det.Nemo  AS "NEMOTECNICO", -- CHAR(16)
		            Det.Fec_Vcto  AS "FECHAVCTOPAPEL", -- CHAR(10)
		            Cab.Fec_Vcto_com  AS "FECHAVCTOPACTO", -- CHAR(10)
		            Det.Tipo_Reaj  AS "TIPOREAJPAPEL", -- CHAR(3)
		            Cab.Tipo_Reaj_Comp  AS "TIPOREAJPACTO", -- CHAR(3)
		            Det.Tir  AS "TASACOMPRA", -- DECIMAL
		            Cab.Tasa_Comprom AS "TASAPACTO", -- DECIMAL
		            Det.Cod_Boveda  AS "CODBOVEDA", -- CHAR(3)
		            '0'  AS "CODRESERVA", -- CHAR(4)
		            0 AS "CANGARANTIA", -- DECIMAL
		            Det.Precio AS "PRECIOPROMCOMPRA", -- DECIMAL
		            Cab.Tipo_Comprom  AS "TIPOCOMPROM", -- CHAR(2)
		            'IF'  AS "MERCADO", -- CHAR(2)
		             (Case When Det.Ind_Fungibilidad = '' Then 'N' Else Det.Ind_Fungibilidad End)  AS "INDFUNGIBILIDAD", -- CHAR(1)
		            ''  AS "RUTEMISOR", -- CHAR(11)
		            Det.Cantidad  AS "CANTIDAD", -- DECIMAL
		           ''  AS "CLASRIESGO", -- CHAR(10)
		          Det.Emisor  AS "EMISOR", -- CHAR(40)
		            Det.Tipo_Cartera  AS "TIPOCARTERA", -- CHAR(3)
		             COALESCE(CAST(INS."FechaEmision" AS VARCHAR(10)),'')  AS "FECHAEMISIONPAPEL", -- CHAR(10) doc."DocumentoIdentidad" 
		           Det.Emisor  AS "CODEMISOR", -- CHAR(10)
		           0  AS "DURATION", -- DECIMAL--- pendiente
		            'CPRVSA    '  AS "CODFONDOLST", -- CHAR(10)
		            ''  AS "RUTCORRLST", -- CHAR(11)
		            0  AS "CUENTACORRLST", -- DECIMAL
		            0  AS "TIRMCDOAYER", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "DURATIONMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "DURATIONPACTO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "CONVEXCOMPRA", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "CONVEXMCDO", -- DECIMAL
		            COALESCE ( 0 , 0 )  AS "CONVEXPACTO", -- DECIMAL
		            0  AS "CODTIPOCARTERA", -- TINYINT
		            0 AS "CODCATIFRSID", -- TINYINT
		            0   AS "INDLIQUIDEZ", -- TINYINT
		            ''  AS "VTOCOMPADJ", -- CHAR(10)
		            0  AS "FACTOR", -- DECIMAL
		            1  AS "CODIGOINTERMEDIADA", -- TINYINT
		            0  AS "CODOFI", -- CHAR(7)
		            0  AS "CODSERIE", -- CHAR(10)
		            0  AS "TIRMED", -- DECIMAL
		            ''  AS "FECVCTOINSTRUMENTO", -- CHAR(10)
		            0 AS "NUMCUPONINSTRUMENTO", -- DECIMAL
		            ''  AS "FAMILIAINSTRUMENTO", -- CHAR(2)
		            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "TIPOCALCULO", -- CHAR(1)
		            COALESCE (CAST('' AS VARCHAR(2)),'')  AS "INDBOLSON", -- CHAR(2)
		            COALESCE (CAST('' AS VARCHAR(1)),'')  AS "ORINEMO" -- CHAR(1)
		    From  "BSCL.Replica.BD.Vistas::Sebra.TBCPMSPC"  Cab     
		    	Inner Join "BSCL.Replica.BD.Vistas::Sebra.TBCPDTPC" Det On (Cab.Sec_Comprom = Det.Sec_Comprom And Det.Mercado ='IF'  
		                                               And Det.Cod_Per=Cab.Cod_per And Det.Cantidad > 0  )
					LEFT JOIN BSCL."BSCL.Plataforma.BD::INS.Instrumento" AS INS ON (INS."Nemotecnico" = Det.Nemo )
					LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIN ON TIN."IdTipoInstrumento" = INS."IdTipoInstrumento"
					LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" AS EMI ON EMI."IdEmisor" = INS."IdEmisor"
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = EMI."IdPersona"
					LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad",
					 "BSCL.Plataforma.BD::PLA.Configuracion" AS fecPro                                                
			   Where 
			   "CodigoParametro" = 'PLA.FechaSistema' 
			   AND Cab.Cod_per= LEFT(fecPro."Valor",7)              
			   And Det.Fec_Liquid > fecPro."Valor"      
			   And Cab.Tipo_Comprom In ('CP','VP')    
			   And Cab.Estado In ('E','P')      and det.SEC_COMPROM =5194341
			   
			   select VAL_RESC ,CANTIDAD ,CANT_PC  from "BSCL.Replica.BD.Vistas::Sebra.TBCPDTPC" brbvst where SEC_COMPROM = 5194341