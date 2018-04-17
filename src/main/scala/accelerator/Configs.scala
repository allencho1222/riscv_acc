package accelerator

import chisel3._
import freechips.rocketchip.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.coreplex._


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
