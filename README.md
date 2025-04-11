# ZeroContact![SAPI](https://github.com/user-attachments/assets/86bf9566-0ff9-4b73-876f-5745025e6659)
Adding ballistic SAPI plates/Body armors to Modded Minecraft providing unique close combat experiences for TACZ(temporary only TACZ)


![JpcBanner_1](https://github.com/user-attachments/assets/c643c721-7ca2-4b74-a3fc-14783b9ebd8b)

## 🪖Features
  - Implements 2 Curio Slots to use plates. Each slot calculates durability and protection by itself.
      - front_plate
      - back_plate
  - Durability and protection mechanism defines the NonLinear way to consume the plate, in detail:
      - Consume durability and take in bullet damages depend on bullet damage amount, plate damage toughness.
        - plates have different bulletproof levels, It will get penetrated if the damage source is strong.
        - Protection is conntected with durability and bullet damage. The more plate get hit, the more durability get consumed.
  - Ricochet!
      - The plate will immune damages if incident angle is big.
