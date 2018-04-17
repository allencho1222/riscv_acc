package Accelerator

import chisel3._
import chisel3.util

import freechips.rocketchip.config._
import freechips.rocketchip.coreplex._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.rocket._
import freechips.rocketchip.tilelink._

class TestAccelerator(implicit p: Parameters) extends LazyRoCC {
  override lazy val module = new TestAcceleratorModule(this)
}

class TestAcceleratorModule(outer: TestAccelerator)(implicit p: Parameters) 
  extends LazyRoCCModule(outer) {

  val regfile = Mem(2, UInt(64))

  // LazyRoCCModule has io
  val cmd = Queue(io.cmd)
  val funct = cmd.bits.inst.funct
  val opcode = cmd.bits.inst.opcode
  val rs1_data = cmd.bits.rs1
  val rs2_data = cmd.bits.rs2

  val do_add = funct === UInt(0)
  val do_sub = funct === UInt(1)
  
  //when writing, rs1 indicates
  val do_load = funct === UInt(2)

  val load_stall = do_load && !io.mem.req.ready

  // fire means producer puts valid data and cosumer is ready to accept it
  when (cmd.fire() && do_sub) {
    io.resp.bits.rd := cmd.bits.inst.rd
    io.resp.bits.data := rs1_data - rs2_data
    regfile(0) := io.resp.bits.data
  } .elsewhen (cmd.fire() && do_add) {
    io.resp.bits.rd := cmd.bits.inst.rd
    io.resp.bits.data := rs1_data + rs2_data
    regfile(1) := io.resp.bits.data
  }

  cmd.ready := !load_stall

  io.interrupt := Bool(false)

  io.mem.req.valid := cmd.valid && do_load
  io.mem.req.bits.cmd := M_XRD
  io.mem.req.bits.typ := MT_D // D = 8bytes, W = 4bytes, H = 2bytes, B = 1bytes
  io.mem.req.bits.data := Bits(0)   // read doesn't write data to cache
  io.mem.req.bits.phys := Bool(false)
  io.mem.invalidate_lr := Bool(false)
  io.mem.req.bits.addr := rs1_data
  io.mem.req.bits.tag := rs2_data
  // TODO: memory address
  when (io.resp.mem.valid) {
    printf("io.mem.resp.bits.data: %d\n", io.mem.resp.bits.data);
    printf("data from address: %08x: %d\n", rs1_data);
  }


  
