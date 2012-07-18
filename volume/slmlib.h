 #pragma once

// slmlib.h

#define SLM_MAX_RANGE			65535	// maximum valid range cm
#define SLM_MAX_RANGE_ACTUAL	40000
#define SLM_MAX_SIGNAL			4095	// maximum valid signal
#define SLM_MAX_ANGLE			36000	// number of angle increments stored

enum SLMstate {
  SLMstate_searching,
  SLMstate_initialising,
  SLMstate_idle,
  SLMstate_scanning
};

#define SLM_PI	3.141592653589793

#define CMD_NEWLINE				'\x0a'
#define CMD_DEVICE_IDENTIFY     '?'		// - sends back "MDA072" string to identify unit online 
#define CMD_ENABLE_FIRING       'A'     // - starts outputing data 
#define CMD_DISABLE_FIRING      'B'     // - stops firing and stops outputing data 
#define CMD_CONFIG				'F'
#define CMD_DO_REPLIES			'H'		// - turn replies on
#define CMD_SET_SPEED			'I'		// - Set speed from 1 to 10 in v2.0+ versions
#define CMD_VER_DATE			'J'		// - Get the version (first units don't answer)
#define CMD_ORIGIN				'O'
#define CMD_POINTER_ON          'P'     // - turn pointer on 
#define CMD_POINTER_OFF         'Q'     // - turn pointer off 
#define CMD_START_MOTOR         'S'     // - starts motor running 
#define CMD_STOP_MOTOR          'T'     // - stops motor running 
#define CMD_NETWORK				'V'
#define CMD_ASCII               'W'		// - enable ASCII output 
#define CMD_BINARY              'X'     // - enable binary output 
#define CMD_READ_STATUS			'Y'

// Older commands
#define CMD_CURRENT_ANGLE       'C'		// - reports current angle position (valid after 1st revolution) 
#define CMD_SPEED				'D'		// - Speed


