import accelerator


class WithCustomAccelerator extends Config((site, here, up) => {
  case RocketTilesKey => up(RocketTilesKey, site).map { r =>
    r.copy(rocc = Seq(
      RoCCParams(
        opcodes = OpcodeSet.custom0 | OpcodeSet.custom1,
        generator = (p: Parameters) => LazyModule(new CustomAccelerator()(p)))))
  }
})

class CustomAcceleratorConfig extends Config(
  new WithCustomAccelerator ++ new BaseConfig)
