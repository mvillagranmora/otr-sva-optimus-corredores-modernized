SET SCHEMA "BSCL";
    
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
--DROP TABLE "BSCL.FondoMutuo.BD::FMM.ConfiguracionAperturaCierre"                  CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.FamiliaFondo"                                   CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.RegistroHistoricoDeSeriesDeFondos"              CASCADE;
--DROP TABLE "BSCL.FondoMutuo.BD::FMM.BloqueoFondo"                                 CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.DividendoEnCuotas"                              CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"                         CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.TramoComision"                                  CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.Comision"                                       CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico"                            CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.ValorCuota"                                     CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.ValorTipoCambioHistorico"                       CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.ValorTipoCambio"                                CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo"                                   CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.Fondo"                                          CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.TipoFondo"                                      CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.Concepto"                                       CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo"                               CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo"                       CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo"                        CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.LogAperturaCierreBloqueo"                       CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.LogCertificacionAporteRescate"                  CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.ArchivoFondoMutuo"                              CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.CategoriaPerfilFondoMutuo"                      CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.DetalleCargaSolicitudesAporteRescate"           CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.Parametro"                                      CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.FormatoArchivo"                                 CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.InterfazAgf"                                    CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.ConfiguracionAdministracionLiquidez"            CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.ConfiguracionPortafolioAdministracionLiquidez"  CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.LogAdministracionLiquidez"                      CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.CarteraOriginalDeInversion"                     CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo"                            CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo"                              CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.EjecucionAdministacionLiquidez"                 CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.ConfiguracionFechasFondoMutuo"                  CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.CuadraturaDiariaOperaciones"					CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.CuadraturaCustodia"								CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.Omnibus"										CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.OmnibusDetalle"									CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino"					CASCADE;
DROP TABLE "BSCL.FondoMutuo.BD::FMM.CrearSolicitudAporteRescateNormal_BloqueoSolicitudAporteRescate"	CASCADE;
/*==============================================================*/
/* Create Table: TABLE                                           */
/*==============================================================*/

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.TipoFondo" (
     "IdTipoFondo"              NVARCHAR(10)    NOT NULL ,
     "NombreCorto"              NVARCHAR(40)    NOT NULL DEFAULT '',
     "Descripcion"              NVARCHAR(100)   NOT NULL DEFAULT '',
     "IndicadorMoneyMarket"     NVARCHAR(1)     NOT NULL DEFAULT 'N',
     PRIMARY KEY ("IdTipoFondo")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" (
    "IdRegimenFondoMutuo"           NVARCHAR(40)    NOT NULL,
    "Descripcion"                   NVARCHAR(100)   NOT NULL,
    "GrupoRegimen"                  NVARCHAR(40)    NOT NULL,
    "PropositoSerieRelacionado"     NVARCHAR(520)   NOT NULL,               
    "PermiteOtrosGrupos"            NVARCHAR(1)     NOT NULL,
    "ValidaPortafolioCliente"       NVARCHAR(1)     NOT NULL,
    "ValidaPortafolioAGF"           NVARCHAR(1)     NOT NULL,
    "PermiteRescate"                NVARCHAR(1)     NOT NULL,
    "TipoCarteraNormativa"          NVARCHAR(6)     NOT NULL,
    ---PARAMETROS PARA REINVERSIONES
    "PermiteCambioGrupo"            NVARCHAR(1)     NOT NULL,
    "RescateConMandato"             NVARCHAR(1)     NOT NULL, --Habilita Check del MLT En Aporte y Rescate REINVERSION
    "AceptaMandatoEnReinversion"    NVARCHAR(1)     NOT NULL    DEFAULT 'N', --Habilita Check del MLT En Rescate por reinversion al cambiar a un grupo distinto                 
    "EsPorDefecto"                  NVARCHAR(1)     NOT NULL DEFAULT 'N',
    "IdEstado"                      NVARCHAR(3)     NOT NULL,
    PRIMARY KEY ("IdRegimenFondoMutuo")     
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo"(
    "IdRegimenFondoMutuo"       NVARCHAR(40)    NOT NULL,
    "IdSerieFondo"              NVARCHAR(40)    NOT NULL,
    "IdEstado"                  NVARCHAR(3)     NOT NULL,
    "IdEmpresa"                 BIGINT          NOT NULL
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" (
     "IdTipoMovimientoFondoMutuo"   NVARCHAR(10)    NOT NULL ,
     "Descripcion"                  NVARCHAR(100)   NOT NULL ,
     "IndicadorAporteRescate"       NVARCHAR(03)    NOT NULL , -- APO:Aporte, RES: Rescate   
     "IdTipoOperacion"              NVARCHAR(06)    NOT NULL ,
     "TieneCarga"                   NVARCHAR(1)     NOT NULL DEFAULT 'N',
     "TieneIngresoManual"           NVARCHAR(1)     NOT NULL DEFAULT 'N',
     "TieneCertificacion"           NVARCHAR(1)     NOT NULL DEFAULT 'N',
     "IdEstado"                     NVARCHAR(3)     NOT NULL ,
     PRIMARY KEY ("IdTipoMovimientoFondoMutuo")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico" (
     "IdValorCuotaHistorico"        BIGINT          NOT NULL ,
     "IdSerieFondo"                 NVARCHAR(40)    NOT NULL ,
     "FechaCierre"                  DATE            NOT NULL ,
     "ValorCuota"                   DECIMAL(26,8)   NOT NULL ,
     "IdMoneda"                     NVARCHAR(3)     NOT NULL ,
     "Observacion"                  NVARCHAR(100)   NOT NULL ,
     "FechaCarga"                   DATE            NOT NULL ,
     "FechaIngresoRegistro"         TIMESTAMP       NOT NULL ,
     "EsUltimaCargaDiaria"          NVARCHAR(3)     NOT NULL    DEFAULT 'N',
     "IdEmpresa"                    BIGINT          NOT NULL ,
     PRIMARY KEY ("IdValorCuotaHistorico")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.ValorCuota" (
     "IdSerieFondo"                 NVARCHAR(40)    NOT NULL ,
     "FechaCierre"                  DATE            NOT NULL ,
     "ValorCuota"                   DECIMAL(26,8)   NOT NULL ,
     "IdMoneda"                     NVARCHAR(3)     NOT NULL ,
     "Observacion"                  NVARCHAR(100)   NOT NULL ,      
     "FechaCarga"                   DATE            NOT NULL ,
     "FechaIngresoRegistro"         TIMESTAMP       NOT NULL ,
     "IdEmpresa"                    BIGINT          NOT NULL ,
     PRIMARY KEY ("IdSerieFondo")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.ValorTipoCambioHistorico" (
     "IdValorTipoCambioHistorico"           BIGINT          NOT NULL ,
     "IdSerieFondo"                         NVARCHAR(40)    NOT NULL ,
     "FechaCierre"                          DATE            NOT NULL ,
     "ValorDolarAporte"                     DECIMAL(26,8)   NOT NULL ,
     "ValorDolarRescate"                    DECIMAL(26,8)   NOT NULL ,
     "Observacion"                          NVARCHAR(100)   NOT NULL ,
     "FechaCarga"                           DATE            NOT NULL ,
     "FechaIngresoRegistro"                 TIMESTAMP       NOT NULL ,
     "IdEmpresa"                            BIGINT          NOT NULL ,
     PRIMARY KEY ("IdValorTipoCambioHistorico")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.ValorTipoCambio" (
     "IdSerieFondo"                 NVARCHAR(40)    NOT NULL ,
     "FechaCierre"                  DATE            NOT NULL ,
     "ValorDolarAporte"             DECIMAL(26,8)   NOT NULL ,
     "ValorDolarRescate"            DECIMAL(26,8)   NOT NULL ,
     "Observacion"                  NVARCHAR(100)   NOT NULL ,      
     "FechaCarga"                   DATE            NOT NULL ,
     "FechaIngresoRegistro"         TIMESTAMP       NOT NULL ,
     "IdEmpresa"                    BIGINT          NOT NULL ,
     PRIMARY KEY ("IdSerieFondo")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.Concepto" (
     "IdConcepto"         BIGINT        NOT NULL ,
     "IdEmpresa"          BIGINT        NOT NULL ,
     "GrupoConcepto"      NVARCHAR(40)  NOT NULL ,
     "CodigoConcepto"     NVARCHAR(40)  NOT NULL ,
     "NombreConcepto"     NVARCHAR(40)  NOT NULL ,
     "Estado"             NVARCHAR(3)   NOT NULL ,
     PRIMARY KEY ("IdConcepto")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" (
     "IdEstadoFondoMutuo" BIGINT        NOT NULL,
     "IdEstado"           NVARCHAR(3)   NOT NULL,
     "NombreEntidad"      NVARCHAR(100) NOT NULL, 
     PRIMARY KEY ("IdEstadoFondoMutuo")
);

/*==============================================================*/
/* Create Table: FONDO                                          */
/*==============================================================*/

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.Fondo" (
     "IdFondo"                                      NVARCHAR(40)    NOT NULL ,
     "IdEmisor"                                     NVARCHAR(40)    NOT NULL ,
     "IdPaisEmisor"                                 BIGINT          NOT NULL ,
     "Nemotecnico"                                  NVARCHAR(40)    NOT NULL ,
     "NombreFondo"                                  NVARCHAR(100)   NOT NULL ,
     "IdTipoFondo"                                  NVARCHAR(10)    NOT NULL , -- Tipo de Fondo para SVS
     "IdPaisOrigen"                                 BIGINT          NOT NULL ,
     "CodigoSVS"                                    NVARCHAR(40)    NOT NULL ,
     "MoneyMarket"                                  NVARCHAR(1)     NOT NULL ,
     "IdCategoriaInversionista"                     NVARCHAR(40)    NOT NULL ,
     "FechaCreacion"                                DATE            NOT NULL ,
     "FechaIngresoRegistro"                         TIMESTAMP       NOT NULL ,
     "FechaConstitucion"                            DATE            NOT NULL ,
     "FechaCierre"                                  DATE            NOT NULL DEFAULT '0001-01-01',
     "Vigencia"                                     NVARCHAR(1)     NOT NULL ,          
     "FondoExtranjero"                              NVARCHAR(1)     NOT NULL ,
     "IdMoneda"                                     NVARCHAR(3)     NOT NULL ,
     "IdTipoInstrumento"                            NVARCHAR(3)     NOT NULL ,
     "IdEmpresa"                                    BIGINT          NOT NULL DEFAULT 0,
     "IdCategoriaPerfilFondoMutuo"                  NVARCHAR(40)    NOT NULL DEFAULT '',
     "UsaHorarioAporteDefault"                      NVARCHAR(1)     NOT NULL DEFAULT 'S' ,
     "UsaHorarioRescateDefault"                     NVARCHAR(1)     NOT NULL DEFAULT 'S' ,
     "UsaHorarioReinversionDefault"                 NVARCHAR(1)     NOT NULL DEFAULT 'S' ,
     "UsaHorarioLiquidezDefault"                    NVARCHAR(1)     NOT NULL DEFAULT 'S' ,
     "HoraCierreAporteComercial"                    TIME            NOT NULL DEFAULT '00:00:00' ,
     "HoraCierreRescateComercial"                   TIME            NOT NULL DEFAULT '00:00:00' ,
     "HoraCierreReinversionComercial"               TIME            NOT NULL DEFAULT '00:00:00' ,
     "HoraCierreLiquidezComercial"                  TIME            NOT NULL DEFAULT '00:00:00' ,
     "HoraCierreAporteBackoffice"                   TIME            NOT NULL DEFAULT '00:00:00' ,
     "HoraCierreRescateBackoffice"                  TIME            NOT NULL DEFAULT '00:00:00' ,
     "HoraCierreReinversionBackoffice"              TIME            NOT NULL DEFAULT '00:00:00' ,
     "HoraCierreLiquidezBackoffice"                 TIME            NOT NULL DEFAULT '00:00:00' ,
     "EstadoAporteComercial"                        NVARCHAR(1)     NOT NULL DEFAULT 'C' ,
     "EstadoRescateComercial"                       NVARCHAR(1)     NOT NULL DEFAULT 'C' ,
     "EstadoReinversionComercial"                   NVARCHAR(1)     NOT NULL DEFAULT 'C' ,
     "EstadoLiquidezComercial"                      NVARCHAR(1)     NOT NULL DEFAULT 'C' ,
     "EstadoAporteBackoffice"                       NVARCHAR(1)     NOT NULL DEFAULT 'C' ,
     "EstadoRescateBackoffice"                      NVARCHAR(1)     NOT NULL DEFAULT 'C' ,
     "EstadoReinversionBackoffice"                  NVARCHAR(1)     NOT NULL DEFAULT 'C' ,
     "EstadoLiquidezBackoffice"                     NVARCHAR(1)     NOT NULL DEFAULT 'C' ,
     "UltimoFechaHoraCierreAporteComercial"         SECONDDATE      NOT NULL DEFAULT '0001-01-01 00:00:00' ,
     "UltimoFechaHoraCierreRescateComercial"        SECONDDATE      NOT NULL DEFAULT '0001-01-01 00:00:00' , 
     "UltimoFechaHoraCierreReinversionComercial"    SECONDDATE      NOT NULL DEFAULT '0001-01-01 00:00:00' ,
     "UltimoFechaHoraCierreLiquidezComercial"       SECONDDATE      NOT NULL DEFAULT '0001-01-01 00:00:00' ,
     "UltimoFechaHoraCierreAporteBackOffice"        SECONDDATE      NOT NULL DEFAULT '0001-01-01 00:00:00' ,
     "UltimoFechaHoraCierreRescateBackOffice"       SECONDDATE      NOT NULL DEFAULT '0001-01-01 00:00:00' , 
     "UltimoFechaHoraCierreReinversionBackOffice"   SECONDDATE      NOT NULL DEFAULT '0001-01-01 00:00:00' ,
     "UltimoFechaHoraCierreLiquidezBackOffice"      SECONDDATE      NOT NULL DEFAULT '0001-01-01 00:00:00' ,
     "UltimoFechaHoraBloqueo"                       SECONDDATE      NOT NULL DEFAULT '0001-01-01 00:00:00' ,
     "BloqueoAporte"                                NVARCHAR(1)     NOT NULL DEFAULT 'N' ,
     "BloqueoRescate"                               NVARCHAR(1)     NOT NULL DEFAULT 'N' ,
     "BloqueoIndefinido"                            NVARCHAR(1)     NOT NULL DEFAULT 'N' ,
     "FechaBloqueoDesde"                            DATE            NOT NULL DEFAULT '0001-01-01' ,
     "FechaBloqueoHasta"                            DATE            NOT NULL DEFAULT '0001-01-01' ,
     "FechaFondo"                                   DATE            NOT NULL DEFAULT '0001-01-01' ,
     "AperturaAutomaticaFondo"                      NVARCHAR(1)     NOT NULL DEFAULT 'S',
     "FondoNormativo"                               NVARCHAR(1)     NOT NULL DEFAULT 'N', --CAMPO PARA DJ1894
     "IdTipoDocumentoIdentidadFondo"                NVARCHAR(3)     DEFAULT '0' NOT NULL ,
     "DocumentoIdentidadFondo"                      NVARCHAR(40)    DEFAULT '' NOT NULL ,
     PRIMARY KEY ("IdFondo")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" (
     "IdSerieFondo"                                 NVARCHAR(40)    NOT NULL ,
     "IdInstrumento"                                BIGINT          NOT NULL ,
     "IdFondo"                                      NVARCHAR(40)    NOT NULL ,
     "NombreSerieFondo"                             NVARCHAR(100)   NOT NULL ,
     "CodigoSerie"                                  NVARCHAR(10)    NOT NULL ,
     "Alias"                                        NVARCHAR(40)    NOT NULL ,
     "DecimalesValorCuota"                          BIGINT          NOT NULL ,
     "DecimalesCuota"                               BIGINT          NOT NULL ,
     "MontoMinimo"                                  DECIMAL(28,6)   NOT NULL ,
     "MontoMaximo"                                  DECIMAL(28,6)   NOT NULL ,
     "DiasRetencionesAporte"                        BIGINT          NOT NULL DEFAULT 0, --nuevo
     "DiasRetencionesRescate"                       BIGINT          NOT NULL DEFAULT 0,
     "DiasValutaCurseAporte"                        BIGINT          NOT NULL DEFAULT 0,
     "DiasValutaCurseRescate"                       BIGINT          NOT NULL DEFAULT 0,
     "DiasValutaValorCuotaAporte"                   BIGINT          NOT NULL DEFAULT 0,--nuevo
     "DiasValutaValorCuotaRescate"                  BIGINT          NOT NULL DEFAULT 0,--nuevo
     "DiasValutaCertificacionAporte"                BIGINT          NOT NULL DEFAULT 0,
     "DiasValutaCertificacionRescate"               BIGINT          NOT NULL DEFAULT 0,
     "ExcluyeGarantia"                              NVARCHAR(1)     NOT NULL ,
     "CobraComision"                                NVARCHAR(1)     NOT NULL ,
     "Diferido"                                     NVARCHAR(1)     NOT NULL ,
     "InstrumentoDCV"                               NVARCHAR(40)    NOT NULL ,
     "Omnibus"                                      NVARCHAR(1)     NOT NULL ,
     "PatrimonioMinimo"                             DECIMAL(28,6)   NOT NULL ,
     "IdSerieFondoOrigen"                           NVARCHAR(40)    NOT NULL ,
     "FechaIngresoRegistro"                         TIMESTAMP       NOT NULL ,
     "IdEstado"                                     NVARCHAR(3)     NOT NULL ,
     "IdCliente"                                    BIGINT          NOT NULL DEFAULT 0 ,
     "IdPortafolio"                                 BIGINT          NOT NULL DEFAULT 0 ,
     "IdMedioDePagoAbono"                           NVARCHAR(3)     NOT NULL DEFAULT '',
     "IdMedioDePagoCargo"                           NVARCHAR(3)     NOT NULL DEFAULT '',
     "IdFamiliaFondo"                               NVARCHAR(40)    NOT NULL DEFAULT '',
     "IdEmpresa"                                    BIGINT          NOT NULL DEFAULT 0 ,
     "CambioFondo"                                  NVARCHAR(1)     NOT NULL DEFAULT 'N',
     "DiasComision"      				BIGINT		     	NOT NULL DEFAULT 0,
  	 "PorcentajeComision"               DECIMAL(26,8) 		NOT NULL DEFAULT 0,
  	 "PorcentajeRemuneracion"           DECIMAL(26,8) 		NOT NULL DEFAULT 0,
  	 "PorcentajeGastoOpera"          	DECIMAL(26,8) 		NOT NULL DEFAULT 0,
  	 "PorcentajeCostoAdm"          		DECIMAL(26,8) 		NOT NULL DEFAULT 0,
     
     PRIMARY KEY ("IdSerieFondo")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.FamiliaFondo" (
     "IdFamiliaFondo" NVARCHAR(40)  NOT NULL ,
     "NombreFamilia"  NVARCHAR(100) NOT NULL ,
     "IdEstado"       NVARCHAR(3)   NOT NULL ,
     "IdEmpresa"      BIGINT        NOT NULL ,   
     PRIMARY KEY ("IdFamiliaFondo")  
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.RegistroHistoricoDeSeriesDeFondos" (
     "IdRegistroHistorico" BIGINT       NOT NULL ,
     "FechaInicioCambio"   DATE         NOT NULL ,
     "IdFondo"             NVARCHAR(40) NOT NULL ,
     "IdSerieFondo"        NVARCHAR(40) NOT NULL ,
     "NombreSerieFondo"    NVARCHAR(40) NOT NULL ,
     "TipoFondoSVS"        NVARCHAR(10) NOT NULL ,
     "IdRegimenTributario" NVARCHAR(10) NOT NULL ,
     "IdPais"              BIGINT       NOT NULL ,
     "CodigoSVS"           NVARCHAR(10) NOT NULL ,
     "IdInstrumento"       BIGINT       NOT NULL ,
     "IdEmpresa"           BIGINT       NOT NULL ,   
     PRIMARY KEY ("IdRegistroHistorico")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.Comision" (
     "IdComision"           BIGINT          NOT NULL ,
     "IdSerieFondo"         NVARCHAR(40)    NOT NULL ,
     "Descripcion"          NVARCHAR(100)   NOT NULL DEFAULT '' ,
     "TipoComision"         NVARCHAR(40)    NOT NULL ,
     "FechaVigenciaDesde"   DATE            NOT NULL ,
     "FechaVigenciaHasta"   DATE            NOT NULL ,
     "FechaSistema"         DATE            NOT NULL ,
     "FechaIngresoRegistro" TIMESTAMP       NOT NULL ,
     "PorcentajeLiberado"   DECIMAL(26,8)   NOT NULL ,
     "Estado"               NVARCHAR(3)     NOT NULL ,
     "IdEmpresa"            BIGINT          NOT NULL ,       
     PRIMARY KEY ("IdComision")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.TramoComision" (
     "IdTramo"              BIGINT          NOT NULL ,
     "IdComision"           BIGINT          NOT NULL ,
     "DiasDesde"            BIGINT          NOT NULL ,
     "DiasHasta"            BIGINT          NOT NULL ,
     "PorcentajeComision"   DECIMAL(26,8)   NOT NULL ,
     "Estado"               NVARCHAR(3)     NOT NULL ,
     "FechaSistema"         DATE            NOT NULL ,
     "FechaIngresoRegistro" TIMESTAMP       NOT NULL ,
     PRIMARY KEY ("IdTramo")
);

/*==============================================================*/
/* Create Table: OPERACIONES                                    */
/*==============================================================*/

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" (
     "IdSolicitudAporteRescate"             BIGINT        NOT NULL ,
     "IdTipoCanalIngreso"                   NVARCHAR(3)   NOT NULL DEFAULT '', --oct
     "FolioContrato"                        BIGINT,--oct
     "IdCliente"                            BIGINT        NOT NULL ,
     "IdPortafolio"                         BIGINT        NOT NULL ,
     "TipoSolicitud"                        NVARCHAR(3)   NOT NULL ,
     "IdSerieFondo"                         NVARCHAR(40)  NOT NULL ,
     "IdTipoMovimientoFondoMutuo"           NVARCHAR(10)  NOT NULL ,     
     "IdDividendo"                          BIGINT        NOT NULL ,
     "IdSerieFondoDestino"                  NVARCHAR(40)  NOT NULL DEFAULT '',
     "IdEmisorDestino"                      NVARCHAR(40)  NOT NULL DEFAULT '',
     "IdClienteDestino"                     BIGINT        NOT NULL DEFAULT 0 ,
     "IdPortafolioDestino"                  BIGINT        NOT NULL DEFAULT 0 ,
     "IdSerieFondoOrigen"                   NVARCHAR(40)  NOT NULL DEFAULT '',
     "IdEmisorOrigen"                       NVARCHAR(40)  NOT NULL DEFAULT '',
     "MontoOrigen"                          DECIMAL(26,8) NOT NULL DEFAULT 0 ,
     "IndicadorMLT"                         NVARCHAR(1)   NOT NULL ,
     "Overnight"                            NVARCHAR(1)   NOT NULL ,
     "IdSolicitudAporteRescateRelacionado"  BIGINT        NOT NULL ,
     "IdTipoCartera"                        NVARCHAR(3)   NOT NULL ,
     "CalculoPorMontoCuota"                 NVARCHAR(1)   NOT NULL ,
     "FechaEmision"                         DATE          NOT NULL ,
     "FechaAceptacion"                      DATE          NOT NULL DEFAULT '0001-01-01',
     "FechaValorCuota"                      DATE          NOT NULL DEFAULT '0001-01-01',
     "FechaCertificacion"                   DATE          NOT NULL DEFAULT '0001-01-01',
     "FechaLiquidacion"                     DATE          NOT NULL DEFAULT '0001-01-01',
     "FechaAnulacion"                       DATE          NOT NULL DEFAULT '0001-01-01',
     "FechaModificacion"                    DATE          NOT NULL DEFAULT '0001-01-01',
     "FechaOriginalInversion"               DATE          NOT NULL ,
     "FechaIngresoRegistro"                 TIMESTAMP     NOT NULL ,
     "FechaAnulacionRegistro"               TIMESTAMP     NOT NULL DEFAULT '0001-01-01 00:00:00.000000',
     "FechaAceptacionRegistro"              TIMESTAMP     NOT NULL DEFAULT '0001-01-01 00:00:00.000000',
     "FechaCertificacionRegistro"           TIMESTAMP     NOT NULL DEFAULT '0001-01-01 00:00:00.000000',
     "FechaLiquidacionRegistro"             TIMESTAMP     NOT NULL DEFAULT '0001-01-01 00:00:00.000000',
     "FechaModificacionRegistro"            TIMESTAMP     NOT NULL DEFAULT '0001-01-01 00:00:00.000000', 
     "EsPorMontoLiquido"                    NVARCHAR(1)   NOT NULL DEFAULT 'N',
     "Monto"                                DECIMAL(26,8) NOT NULL ,
     "NumeroCuotas"                         DECIMAL(26,8) NOT NULL , -- Ver si debe ser decimal
     "Porcentaje"                           DECIMAL(26,8) NOT NULL DEFAULT 0,
     "ValorCuota"                           DECIMAL(26,8) NOT NULL ,
     "CostoUF"                              DECIMAL(26,8) NOT NULL ,
     "CostoMonedaLocal"                     DECIMAL(26,8) NOT NULL ,
     "ParidadFechaCertificacion"            DECIMAL(26,8) NOT NULL DEFAULT 0,
     "ValorUFFechaInversion"                DECIMAL(26,8) NOT NULL ,
     "ParidadFechaInversion"                DECIMAL(26,8) NOT NULL ,
     "IdMoneda"                             NVARCHAR(3)   NOT NULL ,
     "MontoComision"                        DECIMAL(26,8) NOT NULL ,
     "ComisionInformada"                    NVARCHAR(1)   NOT NULL DEFAULT 'N',
     "MontoComisionLocal"                   DECIMAL(26,8) NOT NULL DEFAULT 0,
     "MontoLiquido"                         DECIMAL(26,8) NOT NULL DEFAULT 0 ,
     "MontoLiquidoLocal"                    DECIMAL(26,8) NOT NULL DEFAULT 0 ,
     "MontoPago"                            DECIMAL(26,8) NOT NULL ,
     "MontoPagoLocal"                       DECIMAL(26,8) NOT NULL DEFAULT 0,
     "MontoInversion"                       DECIMAL(26,8) NOT NULL DEFAULT 0,
     "NumeroCuotasInversion"                DECIMAL(26,8) NOT NULL DEFAULT 0,
     "ValorCuotaInversion"                  DECIMAL(26,8) NOT NULL DEFAULT 0,
     "CostoMonedaLocalInversion"            DECIMAL(26,8) NOT NULL DEFAULT 0,
     "Spread"                               DECIMAL(26,8) NOT NULL DEFAULT 0,
     "Estado"                               NVARCHAR(3)   NOT NULL ,
     "EstadoLiquidacion"                    NVARCHAR(3)   NOT NULL DEFAULT 'ING' ,
     "MueveCta"                             NVARCHAR(1)   NOT NULL DEFAULT 'S' ,
     "FolioCartaLiquidacion"                BIGINT        NOT NULL DEFAULT 0 ,
     "IdMedioDePago"                        NVARCHAR(3)   NOT NULL DEFAULT '',
     "Glosa"                                NVARCHAR(100) NOT NULL ,
     "IdEmpresa"                            BIGINT        NOT NULL ,
     "IdUsuario"                            BIGINT        NOT NULL ,
     "IdModulo"                             NVARCHAR(3)   NOT NULL ,
     "OrigenOperacion"                      NVARCHAR(1)   NOT NULL DEFAULT 'M',
     "SolicitudBatch"                       NVARCHAR(1)   NOT NULL DEFAULT 'N',
     "IdRegimenTributario"                  NVARCHAR(40)  NOT NULL DEFAULT '',
     "TipoCarteraNormativa"                 NVARCHAR(6)   NOT NULL DEFAULT '',
     "Tributacion"                          NVARCHAR(40)  NOT NULL DEFAULT '',
     "IdRegimenTributarioDestino"           NVARCHAR(40)  NOT NULL DEFAULT '',
     "TipoCarteraNormativaDestino"          NVARCHAR(6)   NOT NULL DEFAULT '',
     "AdministracionLiquidez"               NVARCHAR(1)   NOT NULL DEFAULT 'N',
     "Visado"                               NVARCHAR(1)   NOT NULL DEFAULT 'N',
     "IdPreferenciaPagoPortafolio"          BIGINT        NOT NULL DEFAULT 0,
     "IdAutorizadoOrigen"                   BIGINT        NOT NULL DEFAULT 0,
     "IdAutorizadoDestino"                  BIGINT        NOT NULL DEFAULT 0,
     "IdMonedaSistema"                      NVARCHAR(3)   NOT NULL DEFAULT '',
     "MontoMonedaSistema"                   DECIMAL(26,8) NOT NULL DEFAULT 0,
     "Switch"      							NVARCHAR(1)   NOT NULL DEFAULT 'N',--Cambio de Serie internacional
     "EstadoAnulacion"						NVARCHAR(4)	  NOT NULL DEFAULT '',
     "FolioOmnibus"							BIGINT 		  NOT NULL DEFAULT 0,
     "FolioAgfOmnibus"						BIGINT 		  NOT NULL DEFAULT 0,
   --  "IdSpotAsociado"                       BIGINT        NOT NULL DEFAULT 0,
   	 "IdAgrupador"							BIGINT		  NOT NULL DEFAULT 0,
     PRIMARY KEY ("IdSolicitudAporteRescate")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.DividendoEnCuotas" (
     "IdDividendo"           BIGINT         NOT NULL ,
     "IdFondo"               NVARCHAR(40)   NOT NULL ,
     "IdSerieFondo"          NVARCHAR(40)   NOT NULL ,
     "MontoDividendo"        DECIMAL(26,8)  NOT NULL ,
     "MonedaDividendo"       NVARCHAR(3)    NOT NULL ,
     "NumeroCuotas"          DECIMAL(26,8)  NOT NULL , -- Ver si debe ser decimal
     "ValorCuota"            DECIMAL(26,8)  NOT NULL ,
     "FechaIngresoDividendo" DATE           NOT NULL ,
     "FechaCierre"           DATE           NOT NULL ,
     "FechaPago"             DATE           NOT NULL ,
     "IdEmpresa"             BIGINT         NOT NULL ,
     "IdUsuario"             BIGINT         NOT NULL ,
     "IdModulo"              NVARCHAR(3)    NOT NULL ,   
     PRIMARY KEY ("IdDividendo")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo" (
    "IdConfiguracionFondoMutuo" BIGINT          NOT NULL ,
    "Key"                       VARCHAR(40)     NOT NULL ,
    "Value"                     VARCHAR(40)     NOT NULL ,
    "IdEmpresa"                 BIGINT          NOT NULL ,
    PRIMARY KEY ("IdConfiguracionFondoMutuo")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.LogAperturaCierreBloqueo"(
    "IdLogAperturaCierreBloqueo"            BIGINT          NOT NULL,
    "IdUsuario"                             BIGINT          NOT NULL,
    "IdEmpresa"                             BIGINT          NOT NULL,
    "IdSerieFondo"                          NVARCHAR(40)    NOT NULL,
    "FechaSistema"                          DATE            NOT NULL,
    "FechaIngresoRegistro"                  TIMESTAMP       NOT NULL,
    "EstadoAporteComercialAntiguo"          NVARCHAR(1)     NOT NULL,
    "EstadoRescateComercialAntiguo"         NVARCHAR(1)     NOT NULL,
    "EstadoReinversionComercialAntiguo"     NVARCHAR(1)     NOT NULL,
    "EstadoLiquidezComercialAntiguo"        NVARCHAR(1)     NOT NULL,
    "EstadoAporteBackofficeAntiguo"         NVARCHAR(1)     NOT NULL,
    "EstadoRescateBackofficeAntiguo"        NVARCHAR(1)     NOT NULL,
    "EstadoReinversionBackofficeAntiguo"    NVARCHAR(1)     NOT NULL,
    "EstadoLiquidezBackofficeAntiguo"       NVARCHAR(1)     NOT NULL,
    "BloqueoAporteAntiguo"                  NVARCHAR(1)     NOT NULL,
    "BloqueoRescateAntiguo"                 NVARCHAR(1)     NOT NULL,
    "BloqueoIndefinidoAntiguo"              NVARCHAR(1)     NOT NULL,
    "FechaBloqueoDesdeAntiguo"              DATE            NOT NULL,
    "FechaBloqueoHastaAntiguo"              DATE            NOT NULL,
    "EstadoAporteComercialNuevo"            NVARCHAR(1)     NOT NULL,
    "EstadoRescateComercialNuevo"           NVARCHAR(1)     NOT NULL,
    "EstadoReinversionComercialNuevo"       NVARCHAR(1)     NOT NULL,
    "EstadoLiquidezComercialNuevo"          NVARCHAR(1)     NOT NULL,
    "EstadoAporteBackofficeNuevo"           NVARCHAR(1)     NOT NULL,
    "EstadoRescateBackofficeNuevo"          NVARCHAR(1)     NOT NULL,
    "EstadoReinversionBackofficeNuevo"      NVARCHAR(1)     NOT NULL,
    "EstadoLiquidezBackofficeNuevo"         NVARCHAR(1)     NOT NULL,
    "BloqueoAporteNuevo"                    NVARCHAR(1)     NOT NULL,
    "BloqueoRescateNuevo"                   NVARCHAR(1)     NOT NULL,
    "BloqueoIndefinidoNuevo"                NVARCHAR(1)     NOT NULL,
    "FechaBloqueoDesdeNuevo"                DATE            NOT NULL,
    "FechaBloqueoHastaNuevo"                DATE            NOT NULL,
    PRIMARY KEY ("IdLogAperturaCierreBloqueo")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.LogCertificacionAporteRescate"(
    "IdLogCertificacionAporteRescate"   BIGINT          NOT NULL ,
    "IdSolicitudAporteRescate"          BIGINT          NOT NULL ,
    "Codigo"                            NVARCHAR(100)   NOT NULL ,
    "IdEstado"                          NVARCHAR(3)     NOT NULL ,
    "TipoSolicitud"                     NVARCHAR(3)     NOT NULL ,
    "IdTipoMovimientoFondoMutuo"        NVARCHAR(10)    NOT NULL ,
    "IdUsuario"                         BIGINT          NOT NULL ,
    "IdEmpresa"                         BIGINT          NOT NULL ,
    "FechaIngresoRegistro"              TIMESTAMP       NOT NULL ,
    "FechaSistema"                      DATE            NOT NULL ,
    "IdSerieFondo"                      NVARCHAR(40)    NOT NULL ,
    "IdEmisor"                          NVARCHAR(40)    NOT NULL ,
    "Monto"                             DECIMAL(26,8)   NOT NULL ,
    "IdMoneda"                          NVARCHAR(3)     NOT NULL ,
    "MontoOriginal"                     DECIMAL(26,8)   NOT NULL ,
    "TipoCambio"                        DECIMAL(26,8)   NOT NULL ,
    "IdMonedaOriginal"                  NVARCHAR(3)     NOT NULL ,
    "ValorCuota"                        DECIMAL(26,8)   NOT NULL ,
    "NumeroCuotas"                      DECIMAL(26,8)   NOT NULL ,
    "IdCliente"                         BIGINT          NOT NULL ,
    "IdPortafolio"                      BIGINT          NOT NULL ,
    "Simulacion"                        NVARCHAR(1)     NOT NULL DEFAULT 'N',
    PRIMARY KEY ("IdLogCertificacionAporteRescate")
);

 CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.DetalleCargaSolicitudesAporteRescate"(
    "IdDetalleCargaSolicitudesAporteRescate"    BIGINT          NOT NULL,
    "IdCargaSolicitudesAporteRescate"           BIGINT          NOT NULL,   
    "RutEmisor"                                 NVARCHAR(40)    NOT NULL,
    "CodigoPortafolioEmisor"                    NVARCHAR(100)   NOT NULL,
    "TipoMovimiento"                            NVARCHAR(10)    NOT NULL,
    "FechaMovimiento"                           DATE            NOT NULL,
    "IdSolicitudAporteRescate"                  BIGINT          NOT NULL,
    "DocumentoIdentidad"                        NVARCHAR(40)    NOT NULL,
    "CodigoPortafolio"                          NVARCHAR(100)   NOT NULL,
    "IdSerieFondo"                              NVARCHAR(40)    NOT NULL,
    "CalculoPorMontoCuota"                      NVARCHAR(1)     NOT NULL,
    "NumeroCuotas"                              DECIMAL(26,8)   NOT NULL,
    "ValorCuota"                                DECIMAL(26,8)   NOT NULL,
    "Monto"                                     DECIMAL(26,8)   NOT NULL,
    "MontoComision"                             DECIMAL(26,8)   NOT NULL,
    "IdMoneda"                                  NVARCHAR(3)     NOT NULL,
    "IdSerieFondoDestino"                       NVARCHAR(40)    NOT NULL,
    "IdCliente"                                 BIGINT          NOT NULL,
    "IdPortafolio"                              BIGINT          NOT NULL,
    "IdEmisor"                                  NVARCHAR(40)    NOT NULL,
    "Concepto"                                  NVARCHAR(10)    NOT NULL DEFAULT '',
    "IndicadorMLT"                              NVARCHAR(1)     NOT NULL DEFAULT 'N',
    "FechaCarga"                                DATE            NOT NULL DEFAULT '0001-01-01',
    "FechaIngresoRegistro"                      TIMESTAMP       NOT NULL DEFAULT '0001-01-01 00:00:00.000000',
    "Estado"                                    NVARCHAR(3)     NOT NULL,
    "DescEstado"                                NVARCHAR(100)   NOT NULL,
    "EstadoProceso"                             NVARCHAR(3)     NOT NULL,
    "Eliminado"                                 NVARCHAR(1)     NOT NULL,
    "IdEmpresa"                                 BIGINT          NOT NULL,
    PRIMARY KEY("IdDetalleCargaSolicitudesAporteRescate")
 );

 CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.ArchivoFondoMutuo"(
    "IdArchivoFondoMutuo"   NVARCHAR(40)    NOT NULL,
    "VersionArchivo"        NVARCHAR(10)    NOT NULL,
    "Esquema"               NVARCHAR(10)    NOT NULL,
    "Version"               NVARCHAR(10)    NOT NULL,
    "IdTipoDocumento"       NVARCHAR(10)    NOT NULL DEFAULT '',
    "Descripcion"           NVARCHAR(100)   NOT NULL DEFAULT '',
    "NombreArchivo"         NVARCHAR(100)   NOT NULL DEFAULT '',
    "IdArchivo"             NVARCHAR(50)    NOT NULL DEFAULT '',
    "TipoArchivo"           NVARCHAR(6)     NOT NULL DEFAULT '',
    "ServicioArchivo"       NVARCHAR(300)   NOT NULL DEFAULT '',
    "IdEmpresa"             BIGINT          NOT NULL DEFAULT 0,
    "IdEstado"              NVARCHAR(3)     NOT NULL DEFAULT '',
    PRIMARY KEY ("IdArchivoFondoMutuo")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.CategoriaPerfilFondoMutuo" (
     "IdCategoriaPerfilFondoMutuo"      VARCHAR(40)     NOT NULL ,
     "IdCategoriaInversionista"         VARCHAR(40)     NOT NULL ,
     "NombreCategoriaPerfilFondoMutuo"  NVARCHAR(100)   NOT NULL ,
     "IdEmpresa"                        BIGINT          NOT NULL ,
     "Jerarquia"                        BIGINT          NOT NULL ,
     PRIMARY KEY ("IdCategoriaPerfilFondoMutuo")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.Parametro"(
    "IdEmpresa"                                 BIGINT CS_FIXED     NOT NULL,
    "IdParametro"                               BIGINT              NOT NULL,
    "NombreTipoParametro"                       NVARCHAR(255)       NOT NULL,
    "ValorParametro"                            NVARCHAR(255)       NOT NULL,
    "NombreParametro"                           NVARCHAR(255)       NOT NULL,
    "IdEstado"                                  NVARCHAR(3)         NOT NULL,
    "EsModificableUsuario"                      NVARCHAR(1)         NOT NULL    DEFAULT 'N',
    "Uso"                                       NVARCHAR(5000),
    "ValorDefecto"                              NVARCHAR(5000),
    "ValoresPosibles"                           NVARCHAR(5000),
    "ModificablePorUsuario"                     NVARCHAR(512),
    "ModificablePostProduccion"                 NVARCHAR(1),
    "CreadorParametro"                          NVARCHAR(512),
    "FechaCreacion"                             SECONDDATE          NOT NULL    DEFAULT '0001-01-01',
    "ModificadorParametro"                      NVARCHAR(512),
    "FechaModificacion"                         SECONDDATE          NOT NULL    DEFAULT '0001-01-01',
    "Observaciones"                             NVARCHAR(5000),
    "Descripcion"                               NVARCHAR(512),
    primary key ("IdParametro")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.FormatoArchivo" (
    "IdFormatoArchivo"      BIGINT          NOT NULL  DEFAULT 0,
    "FormatoSalida"         NVARCHAR(3)     NOT NULL  DEFAULT '', -- TXT CSV
    "Descripcion"           NVARCHAR(100)   NOT NULL  DEFAULT '',
    "IdEmpresa"             BIGINT          NOT NULL  DEFAULT 0,
    "Servicio"              NVARCHAR(250)   NOT NULL  DEFAULT '',
    "IdEstado"              NVARCHAR(3)     NOT NULL  DEFAULT '',
    "RutaArchivo"           NVARCHAR(250)   NOT NULL  DEFAULT '',
PRIMARY KEY ("IdFormatoArchivo") );

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.InterfazAgf" (
    "IdInterfazAgf"                         BIGINT          NOT NULL                                        ,
    "DocumentoIdentidad"                    NVARCHAR(40)    NOT NULL DEFAULT ''                             ,
    "CodigoPortafolio"                      NVARCHAR(100)   NOT NULL DEFAULT ''                             ,
    "FechaLiquidacion"                      DATE            NOT NULL DEFAULT '0001-01-01'                   ,
    "FechaAceptacion"                       DATE            NOT NULL DEFAULT '0001-01-01'                   ,
    "TipoSolicitud"                         NVARCHAR(3)     NOT NULL DEFAULT ''                             ,
    "IdEstadoSolicitud"                     NVARCHAR(3)     NOT NULL DEFAULT ''                             ,
    "IdSerieFondo"                          NVARCHAR(40)    NOT NULL DEFAULT ''                             ,
    "IdUsuario"                             BIGINT          NOT NULL DEFAULT 0                              ,
    "NumeroCuotas"                          DECIMAL(26,8)   NOT NULL DEFAULT 0                              ,
    "ValorCuota"                            DECIMAL(26,8)   NOT NULL DEFAULT 0                              ,
    "Monto"                                 DECIMAL(26,8)   NOT NULL DEFAULT 0                              ,
    "CalculoPorMontoCuota"                  NVARCHAR(1)     NOT NULL DEFAULT ''                             ,
    "IdMoneda"                              NVARCHAR(3)     NOT NULL DEFAULT ''                             ,
    "PropositoSerie"                        NVARCHAR(30)    NOT NULL DEFAULT ''                             ,
    "IdSerieFondoDestino"                   NVARCHAR(40)    NOT NULL DEFAULT ''                             ,
    "IdEstadoSerieFondo"                    NVARCHAR(3)     NOT NULL DEFAULT ''                             ,
    "IndicadorMLT"                          NVARCHAR(1)     NOT NULL DEFAULT ''                             ,
    "IdEstado"                              NVARCHAR(3)     NOT NULL DEFAULT 'PEN'                          ,
    "IdSolicitudAporteRescate"              BIGINT          NOT NULL DEFAULT 0                              ,
    "IdSolicitudAporteRescateRelacionado"   BIGINT          NOT NULL DEFAULT 0                              ,       
    "Observacion"                           NVARCHAR(500)   NOT NULL DEFAULT ''                             ,
    "TipoRegistro"                          NVARCHAR(2)     NOT NULL DEFAULT ''                             ,
    "NombreCompleto"                        NVARCHAR(400)   NOT NULL DEFAULT ''                             ,
    "FechaIngresoRegistro"                  TIMESTAMP       NOT NULL DEFAULT '0001-01-01 00:00:00.000000'   ,
    "FechaIngreso"                          DATE            NOT NULL DEFAULT '0001-01-01'                   ,
    "EstadoBloqueoFondo"                    NVARCHAR(1)     NOT NULL DEFAULT ''                             ,
    "IdCliente"                             BIGINT          NOT NULL DEFAULT 0                              ,
    "IdPortafolio"                          BIGINT          NOT NULL DEFAULT 0                              ,                           
    "IdEmpresa"                             BIGINT                                                          ,
PRIMARY KEY ("IdInterfazAgf") );

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.ConfiguracionAdministracionLiquidez" (
    "IdMoneda"                              NVARCHAR(3)     NOT NULL,
    "MontoMinimo"                           DECIMAL(26,8)   NOT NULL,
    "IdSerieFondo"                          NVARCHAR(40)    NOT NULL,                                       
    "IdEmpresa"                             BIGINT          NOT NULL,
    PRIMARY KEY ("IdMoneda")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.ConfiguracionPortafolioAdministracionLiquidez" (
    "IdConfiguracionPortafolioAdministracionLiquidez"   BIGINT          NOT NULL,
    "IdMoneda"                                          NVARCHAR(3)     NOT NULL,
    "MontoMinimo"                                       DECIMAL(26,8)   NOT NULL,
    "IdSerieFondo"                                      NVARCHAR(40)    NOT NULL,
    "IdCliente"                                         BIGINT          NOT NULL,
    "IdPortafolio"                                      BIGINT          NOT NULL,
    "IdEstado"                                          NVARCHAR(3)     NOT NULL,
    "FechaIngreso"                                      DATE            NOT NULL,
    "FechaIngresoRegistro"                              TIMESTAMP       NOT NULL,
    "FechaModificacion"                                 DATE            NOT NULL    DEFAULT '0001-01-01',
    "FechaModificacionRegistro"                         TIMESTAMP       NOT NULL    DEFAULT '0001-01-01 00:00:00.000000',                                       
    "IdEmpresa"                                         BIGINT          NOT NULL,
    PRIMARY KEY ("IdConfiguracionPortafolioAdministracionLiquidez")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.Depuracion" (
     "IdProceso"          NVARCHAR(500) ,
     "TiempoInicio"       TIME          ,
     "TiempoFin"          TIME          ,
     "Diferencia"         DECIMAL(26,8) ,
     "FechaHora"          TIMESTAMP     
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.LogAdministracionLiquidez"(
    "IdLogAdministracionLiquidez"   BIGINT          NOT NULL,
    "IdCliente"                     BIGINT          NOT NULL,
    "IdPortafolio"                  BIGINT          NOT NULL,
    "IdSerieFondo"                  NVARCHAR(40)    NOT NULL,
    "IdMoneda"                      NVARCHAR(3)     NOT NULL,
    "Saldo"                         DECIMAL(26,8)   NOT NULL,
    "MontoMinimo"                   DECIMAL(26,8)   NOT NULL,
    "IdSolicitudAporteRescate"      BIGINT          NOT NULL,
    "FechaIngreso"                  DATE            NOT NULL,
    "FechaIngresoRegistro"          TIMESTAMP       NOT NULL,
    "Codigo"                        NVARCHAR(100)   NOT NULL,
    "IdEmpresa"                     BIGINT          NOT NULL,
    PRIMARY KEY ("IdLogAdministracionLiquidez")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.CarteraOriginalDeInversion"(
    "IdCarteraOriginalDeInversion"      BIGINT          NOT NULL,
    "IdSolicitudRescateReinversion"     BIGINT          NOT NULL,
    "IdSolicitudAporteReinversion"      BIGINT          NOT NULL,
    "FolioCustodiaRescate"              BIGINT          NOT NULL,
    "FolioCustodiaAporte"               BIGINT          NOT NULL,
    "FechaCartera"                      DATE            NOT NULL,
    "IdConcepto"                        NVARCHAR(6)     NOT NULL,
    "Cantidad"                          DECIMAL(26,8)   NOT NULL,
    "TipoFactorAjuste"                  NVARCHAR(3)     NOT NULL,
    "ValorFactorAjuste"                 DECIMAL(26,8)   NOT NULL,
    "CostoEnFactorAjuste"               DECIMAL(26,8)   NOT NULL,
    "Spread"                            DECIMAL(26,8)   NOT NULL,
    "IndicadorMLT"                      NVARCHAR(1)     NOT NULL,
    PRIMARY KEY ("IdCarteraOriginalDeInversion")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.EjecucionAdministacionLiquidez"(
    "IdMoneda"          NVARCHAR(3) NOT NULL,
    "FechaEjecucion"    DATE        NOT NULL,
    "IdEmpresa"         BIGINT      NOT NULL
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.ConfiguracionFechasFondoMutuo"(
    "IdConfiguracionFechasFondoMutuo"   BIGINT          NOT NULL,
    "IdEmpresa"                         BIGINT          NOT NULL,
    "IdModalidad"                       NVARCHAR(10)    NOT NULL DEFAULT '',--BATCH,UNITARIO,CALENDARIO
    "IdTipoFecha"                       NVARCHAR(6)     NOT NULL DEFAULT '',
    "TipoSolicitud"                     NVARCHAR(3),
    "EstadoFondo"                       NVARCHAR(1),
    "FondoExtranjero"                   NVARCHAR(1),
    "Descripcion"                       NVARCHAR(100)   NOT NULL,
    --------------------------------------------------------------------------
    "CantidadDias"                      BIGINT          NOT NULL DEFAULT 0,
    "EsHabil"                           NVARCHAR(1)     NOT NULL DEFAULT 'S',
    "IdTipoFechaOrigen"                 NVARCHAR(6),
    "DebeSerHabilFechaOrigen"           NVARCHAR(1)     NOT NULL DEFAULT 'N',--Se valida si la fecha origen debe ser habil o no
    "ValutaCurse"                       NVARCHAR(1)     NOT NULL DEFAULT 'N',
    "ValutaValorCuota"                  NVARCHAR(1)     NOT NULL DEFAULT 'N',
    "ValutaCertificacion"               NVARCHAR(1)     NOT NULL DEFAULT 'N',
    "ValutaRetencion"                   NVARCHAR(1)     NOT NULL DEFAULT 'N',
    "CalendarioLocal"                   NVARCHAR(1)     NOT NULL DEFAULT 'S',
    "CalendarioEmisor"                  NVARCHAR(1)     NOT NULL DEFAULT 'N',
    "CalendarioFondo"                   NVARCHAR(1)     NOT NULL DEFAULT 'N',
    "CalendarioMoneda"                  NVARCHAR(1)     NOT NULL DEFAULT 'N',
    "IdEstado"                          NVARCHAR(3)     NOT NULL DEFAULT 'ACT',
    PRIMARY KEY ("IdConfiguracionFechasFondoMutuo")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.CuadraturaDiariaOperaciones" (
	"IdCuadraturaDiariaOperaciones"			BIGINT			NOT NULL,
	"IdEmpresa"								BIGINT			NOT NULL DEFAULT 0,
	"OperacionCuadraturaDiariaOperaciones"	NVARCHAR(200) 	NOT NULL DEFAULT '',
	"FechaEjecucion"						DATE			NOT NULL DEFAULT '0001-01-01 00:00:00',	
	"TotalOperaciones"						BIGINT			NOT NULL DEFAULT 0,
	"OperacionesConProblemas"				BIGINT			NOT NULL DEFAULT 0,
	"TipoOperacion"							NVARCHAR(10) 	NOT NULL DEFAULT '',
	"Resultado"								NVARCHAR(100) 	NOT NULL DEFAULT '',
	PRIMARY KEY ("IdCuadraturaDiariaOperaciones")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.CuadraturaCustodia"(
	"IdCuadraturaCustodia"		BIGINT NOT NULL,
	"IdEmpresa"					BIGINT NOT NULL,
	"IdSerieFondo"				NVARCHAR(40) NOT NULL,
	"TotalCuotasAGF"			DECIMAL(26,8) NOT NULL DEFAULT 0,
	"TotalCuotas"				DECIMAL(26,8) NOT NULL DEFAULT 0,
	"DiferenciaCuotas"			DECIMAL(26,8) NOT NULL DEFAULT 0,
	"ValorizacionCuotasAGF" 	DECIMAL(26,8) NOT NULL DEFAULT 0,
	"ValorizacionCuotas"		DECIMAL(26,8) NOT NULL DEFAULT 0,
	"DiferenciaValorizacion" 	DECIMAL(26,8) NOT NULL DEFAULT 0,
	"FechaCuadratura"			DATE NOT NULL DEFAULT '0001-01-01',
	"FechaIngresoRegistro"		TIMESTAMP NOT NULL DEFAULT '0001-01-01 00:00:00.000000',
	"ValorCuota"				DECIMAL(26,8) NOT NULL DEFAULT 0,
	"IdRegimen"					NVARCHAR(40) NOT NULL DEFAULT '',
	"IdEstado"					NVARCHAR(3) NOT NULL,
	PRIMARY KEY ("IdCuadraturaCustodia")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.Omnibus"(
	"IdOmnibus"					BIGINT NOT NULL,
	"FechaOmnibus"				DATE NOT NULL DEFAULT '0001-01-01',
	"FechaIngresoRegistro"		TIMESTAMP NOT NULL DEFAULT '0001-01-01 00:00:00.000000',
	"IdEstado"					NVARCHAR(3) NOT NULL DEFAULT '',
	PRIMARY KEY ("IdOmnibus")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.OmnibusDetalle"(
	"IdOmnibusDetalle"			BIGINT NOT NULL,
	"IdOmnibus"					BIGINT NOT NULL,
	"IdSerieFondo"				NVARCHAR(40) NOT NULL,
	"CantidadOperaciones"		BIGINT NOT NULL,
	"FechaCertificacion"		DATE NOT NULL,
	"ValorCuota"				DECIMAL(26,8) NOT NULL DEFAULT 0,
	"UltimoValorCuota"			DECIMAL(26,8) NOT NULL DEFAULT 0,
	"Monto"						DECIMAL(26,8) NOT NULL DEFAULT 0,
	"CantidadCuotas"			DECIMAL(26,8) NOT NULL DEFAULT 0,
	"ValorCuotaAGF"				DECIMAL(26,8) NOT NULL DEFAULT 0,
	"MontoAGF"					DECIMAL(26,8) NOT NULL DEFAULT 0,
	"CantidadCuotasAGF"			DECIMAL(26,8) NOT NULL DEFAULT 0,
	"DiferenciaValorCuota"		DECIMAL(26,8) NOT NULL DEFAULT 0,
	"DiferenciaValorCuotaAGF"	DECIMAL(26,8) NOT NULL DEFAULT 0,
	"IdEstado"					NVARCHAR(3) NOT NULL DEFAULT '',
	PRIMARY KEY ("IdOmnibusDetalle")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino" (
	"IdReinversionSerieFondo"		BIGINT NOT NULL,
	"IdSolicitudAporteRescate" 		BIGINT NOT NULL,
	"IdSerieFondoDestino" 			NVARCHAR(40) NOT NULL,
	"Porcentaje"					DECIMAL(26,8) NOT NULL DEFAULT 0,
	PRIMARY KEY ("IdReinversionSerieFondo")  
);

CREATE COLUMN TABLE "BSCL"."BSCL.FondoMutuo.BD::FMM.CrearSolicitudAporteRescateNormal_BloqueoSolicitudAporteRescate" (
	"IdBloqueoSolicitudAporteRescate" 	NVARCHAR(120),
	"IdPortafolio" 						BIGINT CS_FIXED,
	"IdSerieFondo" 						NVARCHAR(40),
	"IdRegimenTributario" 				NVARCHAR(40),
	"TipoSolicitud" 					NVARCHAR(3),
	"Monto" 							DECIMAL(26,8),
	"NumeroCuotas" 						DECIMAL(26,8),
	"FechaSistema" 						DATE,
	PRIMARY KEY ("IdBloqueoSolicitudAporteRescate")
);

/*
CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.BloqueoFondo" (
     "IdBloqueo"            BIGINT       NOT NULL ,
     "IdFondo"              NVARCHAR(40) NOT NULL ,
     "Indefinido"           NVARCHAR(1)  NOT NULL ,
     "FechaInicio"          DATE         NOT NULL ,
     "FechaHasta"           DATE         NOT NULL ,
     "IdMotivoBloqueo"      BIGINT       NOT NULL ,
     "BloqueoAporte"        NVARCHAR(1)  NOT NULL ,
     "BloqueoRescate"       NVARCHAR(1)  NOT NULL ,
     "FechaRegistroBloqueo" DATE         NOT NULL ,
     "IdEstadoBloqueo"      NVARCHAR(1)  NOT NULL ,
     PRIMARY KEY ("IdBloqueo")
);




CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.ConfiguracionAperturaCierre" (
     "IdConfiguracion"              NVARCHAR(40) NOT NULL ,
     "AlcanceConfiguracion"         NVARCHAR(40) NOT NULL ,
     "HoraCierreRescate"            TIME         NOT NULL ,
     "HoraCierreAporte"             TIME         NOT NULL ,
     "HoraCierreReinversion"        TIME         NOT NULL ,
     "IdFondo"                      NVARCHAR(40) NOT NULL ,
     "IdEmisor"                     NVARCHAR(40) NOT NULL ,
     "FondoExtranjero"              NVARCHAR(1)  NOT NULL ,
     "IdPerfil"                     NVARCHAR(40) NOT NULL ,
     PRIMARY KEY ("IdConfiguracion")
);

CREATE COLUMN TABLE "BSCL.FondoMutuo.BD::FMM.ConfiguracionPerfilFondoMutuo" (
     "IdConfiguracionPerfilFondoMutuo"   NVARCHAR(40)   NOT NULL ,
     "Descripcion"                       NVARCHAR(100)  NOT NULL ,
     "IdPerfil"                          BIGINT         NOT NULL ,
     PRIMARY KEY ("IdConfiguracionPerfilFondoMutuo")
);*/


/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/

/* Table: ConfiguracionAperturaCierre */

/*ALTER TABLE "BSCL.FondoMutuo.BD::FMM.ConfiguracionAperturaCierre"
    ADD CONSTRAINT "FK_ConfiguracionAperturaCierre_Fondo" FOREIGN KEY ("IdFondo")
        REFERENCES "BSCL.FondoMutuo.BD::FMM.Fondo" ("IdFondo") ON DELETE RESTRICT ON UPDATE RESTRICT;*/


/* Table: BloqueoFondo */

/*ALTER TABLE "BSCL.FondoMutuo.BD::FMM.BloqueoFondo"
    ADD CONSTRAINT "FK_BloqueoFondo_Fondo" FOREIGN KEY ("IdFondo")
        REFERENCES "BSCL.FondoMutuo.BD::FMM.Fondo" ("IdFondo") ON DELETE RESTRICT ON UPDATE RESTRICT;*/

/* Table: SerieDeFondo */

ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SerieDeFondo"
    ADD CONSTRAINT "FK_SerieDeFondo_Fondo" FOREIGN KEY ("IdFondo")
        REFERENCES "BSCL.FondoMutuo.BD::FMM.Fondo" ("IdFondo") ON DELETE RESTRICT ON UPDATE RESTRICT;
        
/* Table: RegistroHistoricoDeSeriesDeFondos */

ALTER TABLE "BSCL.FondoMutuo.BD::FMM.RegistroHistoricoDeSeriesDeFondos"
    ADD CONSTRAINT "FK_RegistroHistoricoDeSeriesDeFondos_SerieDeFondo" FOREIGN KEY ("IdSerieFondo")
        REFERENCES "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ("IdSerieFondo") ON DELETE RESTRICT ON UPDATE RESTRICT;

/* Table: SolicitudAporteRescate */

ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
    ADD CONSTRAINT "FK_SolicitudAporteRescate_SerieDeFondo" FOREIGN KEY ("IdSerieFondo")
        REFERENCES "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ("IdSerieFondo") ON DELETE RESTRICT ON UPDATE RESTRICT;

/*ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
    ADD CONSTRAINT "FK_SolicitudAporteRescate_DividendoEnCuotas" FOREIGN KEY ("IdDividendo")
        REFERENCES "BSCL.FondoMutuo.BD::FMM.DividendoEnCuotas" ("IdDividendo") ON DELETE RESTRICT ON UPDATE RESTRICT;*/

ALTER TABLE "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" 
    ADD CONSTRAINT "FK_SolicitudAporteRescate_TipoMovimientoFondoMutuo" FOREIGN KEY ("IdTipoMovimientoFondoMutuo")
        REFERENCES "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" ("IdTipoMovimientoFondoMutuo") ON DELETE RESTRICT ON UPDATE RESTRICT;
         
/* Table: Comision */

ALTER TABLE "BSCL.FondoMutuo.BD::FMM.Comision"
    ADD CONSTRAINT "FK_Comision_SerieDeFondo" FOREIGN KEY ("IdSerieFondo")
        REFERENCES "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ("IdSerieFondo") ON DELETE RESTRICT ON UPDATE RESTRICT;

/* Table: TramoComision */

ALTER TABLE "BSCL.FondoMutuo.BD::FMM.TramoComision"
    ADD CONSTRAINT "FK_TramoComision_Comision" FOREIGN KEY ("IdComision")
        REFERENCES "BSCL.FondoMutuo.BD::FMM.Comision" ("IdComision") ON DELETE RESTRICT ON UPDATE RESTRICT;
        
/* Table PrecioSerideDeFondo */

ALTER TABLE "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico"
    ADD CONSTRAINT "FK_ValorCuotaHistorico_SerieDeFondo" FOREIGN KEY ("IdSerieFondo")
        REFERENCES "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ("IdSerieFondo") ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE "BSCL.FondoMutuo.BD::FMM.ValorCuota"
    ADD CONSTRAINT "FK_ValorCuota_SerieDeFondo" FOREIGN KEY ("IdSerieFondo")
        REFERENCES "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ("IdSerieFondo") ON DELETE RESTRICT ON UPDATE RESTRICT;   
        
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.ValorTipoCambioHistorico"
    ADD CONSTRAINT "FK_ValorTipoCambioHistorico_SerieDeFondo" FOREIGN KEY ("IdSerieFondo")
        REFERENCES "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ("IdSerieFondo") ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE "BSCL.FondoMutuo.BD::FMM.ValorTipoCambio"
    ADD CONSTRAINT "FK_ValorTipoCambio_SerieDeFondo" FOREIGN KEY ("IdSerieFondo")
        REFERENCES "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ("IdSerieFondo") ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo"
    ADD CONSTRAINT "FK_RegimenSerieDeFondo_SerieDeFondo" FOREIGN KEY ("IdSerieFondo")
        REFERENCES "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ("IdSerieFondo") ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo"
    ADD CONSTRAINT "FK_RegimenSerieDeFondo_RegimenFondoMutuo" FOREIGN KEY ("IdRegimenFondoMutuo")
        REFERENCES "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" ("IdRegimenFondoMutuo") ON DELETE RESTRICT ON UPDATE RESTRICT;   

ALTER TABLE "BSCL.FondoMutuo.BD::FMM.OmnibusDetalle"
    ADD CONSTRAINT "FK_OmnibusDetalle_Omnibus" FOREIGN KEY ("IdOmnibus")
        REFERENCES "BSCL.FondoMutuo.BD::FMM.Omnibus" ("IdOmnibus") ON DELETE RESTRICT ON UPDATE RESTRICT;
        
/* Table: ReinversionSerieFondoDestino */
		
ALTER TABLE "BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino"
	ADD CONSTRAINT "FK_ReinversionSerieFondoDestino_SerieDeFondo" FOREIGN KEY ("IdSerieFondoDestino")
		REFERENCES "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" ("IdSerieFondo") ON DELETE RESTRICT ON UPDATE RESTRICT;

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
COMMIT;
