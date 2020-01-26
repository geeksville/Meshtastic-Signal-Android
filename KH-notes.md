# Signal investigation!

Note: the android app for talking to the mesh radio is very much a WIP in progress.  But if you are curious [here it is](https://github.com/geeksville/Meshtastic-Android).  The embedded code in the radio is also a WIP and I'll be posting it to github soon.

The existing Signal code is super clean and nice.

## Send message flow

* if the user sends "hello" as text
* before encrypting the message it is a "Content" protobuf about 46 bytes long: "data_message {
  body: "hello"
  profile_key: "\265\235\331\001a\027i\200$f\026\275\030\021\335\000\302\335\037\330\355A3m\237G\221$\300A\223\254"
  timestamp: 1580078077480
}"
* an encrypted message to one person is 1464 bytes for a four letter message (with the json encoding and base64 overhead).  after removing base64 it is 549 bytes.
* sample (from SignalServiceMessagePipe.send requestMessage.body: {"destination":"+1650xxxyyyy","messages":[{"content":"EQohBbfWS... shortened ... prIWCUlsrr","destinationDeviceId":1,"destinationRegistrationId":14580,"type":6}],"online":false,"timestamp":1579889398714}
* first the text is encapsulated as a Content protobuf.
* then it is encrypted in SignalServiceMessageSender.getEncryptedMessages
* provide override for SignalServiceMessagePipe.send, the full request/payload can be seen at like 131 of that file
* recipient.e164 contains the phone number
* the full encrypted message text comes in list.messages[].content.  Sample content seems to be already base64 encoded:
EQohBbfWSMUfxuSj0Wj7eaod0LBKulQ4CGM7ZrpvqYr+2oNkEitt8DDDRRgM4sJseMQQRfD3e2IKo+rHxyy18ZLvMnjepZQwRVqRywgGNui3GtEDOLEtUtbT6uq67hJS/KafZTYmc2e/G/FgLE2bhnsXUfyIkzubRRyKrivBGQ4JEInWZJb4U4Qbgd/3TZgWBnB7mPcLTC7EE0s+T/nZtMtHn4Y/fgroISnnhxcxE3eqnToHJ5ZFMZr66fPmpMdujMWrwX0z+M4bJ2hunlG5C1zkVcuArvxD+aUf1DZOZBp5NV0sceCmZqomm6awTuxiXxjAs6kYeXu0r6fOx8pinlzRXUxe9J/Z9ZNmwUt388atrUMW56SSSGnalcTUKgDCKu5bcje4WYU03Ni5j9vVsQiN0TbK1msOirtCx7f4DbMkD0b7yYUwVqPxTrqFq1VDOfsmf02VDNy4rBauQCH5rSqKG7BIpAaWA3SXkfIPwjrHiQeMGCXMrcrEcbqj/1GVAbLIjsoKwiQXiI1i1bV0TGnPtMIOls1e6BtJnppm4H/kqX8SwffajPWQEqQ6vRk4R6hsSFQ+04BYg6MfYxIPVD4+9u7SL9YYEHU3e1/yVE4Rn/kRbZqoPuy8s1lWm0X7pZjYZN02cIDG69D51I6hWL6dKtVZ2UVATVwsRWSrs4CrgP5BmIymPgFYCsJLckIcBDQLEXEkXFnaH+6FYoprIWCUlsrr

## Receive message flow

* Runs inside a MessageRetreivalService thread, does a blocking read from the web server in SignalServiceMessagePipe.read - line 102 (websocket.readRequest)
* line 108 of SignalServiceMessagePipe creates a signal envelope based on the received json, headers, etc... (note: the payload is not decrypted at this point - except for 
the extra optional signalKeyEncrypted flag for a trusted client impl talking to a trusted server?)
* the message then gets passed through a series of decryption etc... jobs before getting broadcast as an Intent and shown in the GUI

# Implementation plan

Here's my current thoughts.  Any feedback would be appreciated!  The related work on the GPS+mesh radios (both the android app side and the device side side is going well).
I think my original guess of an initial release in about a month is correct.  With optional Signal support coming shortly thereafter. 

If anyone is curious about the device side code I'm happy to provide github links, discussion etc...

## Proof of concept
This would be the initial release of this modified version of Signal-Android.  (I would list it on the play store for alpha users as Meshtastic-Signal? if I can get it working...)

Send message path:
* SignalServiceMessagePipe is getting created on L205 (for the 'regular' pipe) and L214 (for 'unidentified' pipes FIXME, what does this mean?) of SignalServiceMessageReceiver 
change to create MeshOverlayMessagePipe
* PROBLEM - oops - SignalServiceMessageReceiver is in the platform independent java lib.  So I can't easily call out there to make my (android using) overlay.
* provideSignalServiceMessageReceiver in ApplicationDependency provider might be a good place to hook this?
* note IncomingMessageObserver seems to provide the thread that repeatedly blocks reading from the SignalServiceMessagePipe (L102 of SignalServiceMessagePipe is where we stall reading from the web socket)

* Solution (for this small experiment, final solution if this becomes real is different - best I think to abstract the websocketish stuff into a WebsocketTransport and make a new MeshTransport?): 
* in provideSignalServiceMessageReceiver create a subclass of SignalServiceMessageReceiver: MeshOverlayMessageReceiver which has createUnidentifiedMessagePipe and createMessagePipe modified to return MeshOverlayMessagePipe
* override for SignalServiceMessagePipe.send, the full request/payload can be seen at like 131 of that file

Receive message path:
* override SignalServiceMessagePipe.read to create a SignalServiceEnvelope based on messages that arrive (via android intent broadcast from the mesh radio app).

## Extra work for real implementation
There are a number of changes which would be needed to make this project more appealing and more cleanly (optionally) integrate with Signal

* Possibly destructure the Content protobuf for optimized text sending (and smaller) which can be understood by the radios? (no security though)
* show an insecure icon if the user has opted to use the mesh with only the mesh level crypto turned on (instead of the blue secure signal icon)
* Instead of my crufty MeshOverlayMessagePipe experiment, add the (thin/small) notion of a Transport and move the REST stuff from SignalServiceMessagePipe into 
RESTTransport and put my mesh stuff in MeshTransport (this would nicely allow different choices for encryyption, encoding, framing and transport in one smallish abstraction)
* make online/offline/find current users in mesh work correctly
* undo the (base64?) encoding to send fewer bytes over the wire (see sample content)
* compress the payloads? (FIXME, possibly low savings due to their already being encrypted/encoded?)
* consider pro/cons and possible ways to shrink message payload size.  In SignalServiceCipher.encrypt if destination is inside the mesh, possibly use weaker encryption in
exchange for brevity and range.

# Questions for signal devs

These are not critical questions and I admit I haven't yet looked myself to find the answers in the code, but if anyone easily knows this off the top of their head any
feedback would help my understanding.

In SignalServiceAddress
* What is the role of relay and uuid?  i.e. what are they typically used for / what is design intent?

