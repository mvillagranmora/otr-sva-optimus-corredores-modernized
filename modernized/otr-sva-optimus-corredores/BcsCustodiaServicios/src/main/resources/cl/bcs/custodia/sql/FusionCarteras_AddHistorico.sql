INSERT INTO "BSCL.Custodia.BD::HIS.FusionCartera"
( "FechaFusion", "CantidadFusion", "TasaFusion", "ValorRescateFusion",
"IdClasificacionTipoInstrumento", "MontoFusion", "MontoValorizado",
"IdCarteraResultante", "IdModulo", "IdUsuario", "IdEmpresa")
VALUES(:FechaFusion,:CantidadFusion, :TasaFusion, :ValorRescateFusion,
:IdClasificacionTipoInstrumento, :MontoFusion, :MontoValorizado,
:IdCarteraResultante, :IdModulo, :IdUsuario, :IdEmpresa);