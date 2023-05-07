using System;
using System.Collections.Generic;
using UnityEngine;
using Fusion;
using UnityEngine.UIElements;
using UnityEngine.UI;
using TMPro;
using System.Security.Cryptography;
using WebGLSupport;

public enum ChatType { Normal = 0, Party, Guild, Whisper, System, Count }
public class ChatController : NetworkBehaviour
{
    [SerializeField]
    private UIDocument doc;
    private VisualElement chat;
    public PlayerMovement player;

    public string username = "Guest";

    [Header("GUI chatting")]
    [SerializeField]
    private GameObject chatContent;
    [SerializeField]
    private TMP_InputField inputChat;
    [SerializeField]
    private TMP_Text outputChat;
    [SerializeField]
    private UnityEngine.UI.Button sendChat;
    [SerializeField]
    private GameObject textChatPrefab;
    [SerializeField]
    private Transform parentContent;
    private bool chatboxVisibility = false;
    private GameObject speechBubble;
    private TMP_Text speechBubbleText;

    [Header("chat type")]
    [SerializeField]
    private Sprite[] spriteChatInputType; // 대화 속성에 해당하는 이미지 에셋
    [SerializeField]
    private UnityEngine.UI.Image imageChatInputType; // 대화 속성 이미지
    [SerializeField]
    private TextMeshProUGUI textInput;

    private ChatType currentInputType; // 현재 대화 속성
    private Color currentTextColor; // 입력에 따라 색상 변환

    private List<ChatCell> chatList; //대화창에 출력되는 모든 대화를 보관
    private ChatType currentViewType; //현재 대화 보기 속성

    [Header("whisper")]
    private string lastWhisperID = ""; // 마지막 귓말 대상
    private string friendID = "Friend";//친구 아이디;

    private void Awake()
    {
        chatList = new List<ChatCell>();

        currentInputType = ChatType.Normal;
        currentTextColor = Color.black;
    }

    private void Start()
    {
        chat = doc.rootVisualElement.Q<VisualElement>("Chat");

        chat.AddManipulator(new Clickable(ChatOnClicked));
    }

    public void Update()
    {
        if (Input.GetKeyDown(KeyCode.KeypadEnter) || Input.GetKeyDown(KeyCode.Return))
        {
            inputChat.Select();

            Debug.Log("enter");
            if (chatboxVisibility)
            {
                if (inputChat.text == "")
                {
                    ChatOnClicked();
                    return;
                }
                Debug.Log("send Message");
                SendMessage();
            }
            else
            {
                ChatOnClicked();
            }

            //대화 입력창 포커스 활성화;
            inputChat.ActivateInputField();
        }

        if (Input.GetKeyDown(KeyCode.Tab) && chatboxVisibility)
        {
            SetCurrentInputType();
        }
    }

    private void ChatOnClicked()
    {
        Debug.Log("clicked");

        if (chatboxVisibility)
        {
            chatContent.SetActive(false);
        }
        else
        {
            chatContent.SetActive(true);
        }

        chatboxVisibility = !chatboxVisibility;

        player.stop = chatboxVisibility ? true : false;
    }

    public void SetUserName(string text)
    {
        username = text;
    }

    public void SendMessage()
    {
        Debug.Log(inputChat.text);
        RPCSendMessage(username, inputChat.text, currentInputType);
        if (currentInputType == ChatType.Normal)
        {
            ActiveSpeechBubble(inputChat.text);
        }
        inputChat.Select();
        inputChat.text = "";

    }

    [Rpc(RpcSources.All, RpcTargets.All)]
    public void RPCSendMessage(string username, string message, ChatType senderInputType, RpcInfo rpcInfo = default)
    {
        Debug.Log("<=" + message);
        Debug.Log("sender input type:" + senderInputType);

        UpdateChatWithCommand(username, message, senderInputType);
    }

    public void ActiveSpeechBubble(string message)
    {
        if(!speechBubble)
        {
            speechBubble = player.transform.GetChild(0).GetChild(2).GetChild(1).gameObject;
            speechBubbleText = speechBubble.transform.GetChild(0).GetComponent<TMP_Text>();
        }

        speechBubble.SetActive(true);
        speechBubbleText.text = message;
    }

    public void PrintChatData(string username, string message, ChatType type, Color color)
    {
        GameObject clone = Instantiate(textChatPrefab, parentContent);
        ChatCell cell = clone.GetComponent<ChatCell>();

        cell.SetUp(type, color, $"{username}: {message}\n");

        chatList.Add(cell);
    }

    public void UpdateChatWithCommand(string username, string message, ChatType senderInputType)
    {
        if (!message.StartsWith('/'))
        {
            PrintChatData(username, message, senderInputType, ChatTypeToColor(senderInputType));
            return;
        }

        //귓말
        if (message.StartsWith("/w"))
        {

            //명령어, 귓말대상, 내용
            string[] whisper = message.Split(' ', 3);

            //모든 유저의 아이디를 검색에 동일한 아이디가 있는지 검사 후
            //대상이 있으면 보내고 없으면 시스템 메세지 출력
            if (whisper[1] == friendID)
            {
                lastWhisperID = whisper[1];

                PrintChatData(username, $"[to {whisper[1]}] {whisper[2]}", ChatType.Whisper, ChatTypeToColor(ChatType.Whisper));
            }
            else
            {
                PrintChatData("[system]", $"[{whisper[1]}]님을 찾지 못했습니다", ChatType.System, ChatTypeToColor(ChatType.System));
            }
        }
        //마지막에 귓말을 보낸 대상에게 다시 귓말 보내기
        else if (message.StartsWith("/r"))
        {
            if (lastWhisperID.Equals(""))
            {
                inputChat.text = "";
                return;
            }

            string[] whisper = message.Split(' ', 2);

            PrintChatData(username, $"[to {lastWhisperID}] {whisper[1]}", ChatType.Whisper, ChatTypeToColor(ChatType.Whisper));
        }
    }

    private Color ChatTypeToColor(ChatType type)
    {
        Color[] colors = new Color[(int)ChatType.Count] {
            Color.black, Color.blue, Color.green, Color.magenta, Color.yellow
        };

        return colors[(int)type];
    }

    public void SetCurrentInputType()
    {
        //현재 대화 속성을 한 단계씩 변화(귓말, 시스템은 입력 속성에 없기 때문에 제외)
        currentInputType = (int)currentInputType < (int)ChatType.Count - 3 ? currentInputType + 1 : 0;
        //버튼 이미지 변경
        imageChatInputType.sprite = spriteChatInputType[(int)currentInputType];
        //텍스트 색상 변경
        currentTextColor = ChatTypeToColor(currentInputType);
        //대화 입력창의 텍스트 생상 변경
        textInput.color = currentTextColor;
    }

    public void SetCurrentViewType(int newType)
    {
        //Button UI의 OnClick 이벤트에 열거형은 매개변수로 처리가 안되서 int로 받아온다.
        currentViewType = (ChatType)newType;

        if (currentViewType == ChatType.Normal)
        {
            //모든 대화 목록 활성화
            for (int i = 0; i < chatList.Count; ++i)
            {
                chatList[i].gameObject.SetActive(true);
            }
        }
        else
        {
            //현재 대화 보기 설정만 활성화
            for (int i = 0; i < chatList.Count; ++i)
            {
                chatList[i].gameObject.SetActive(chatList[i].ChatType == currentViewType);
            }
        }
    }
}
