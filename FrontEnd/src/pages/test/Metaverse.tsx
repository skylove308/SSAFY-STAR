import { Unity, useUnityContext } from "react-unity-webgl";
import FloatingMenu from "../../components/Layout/FloatingMenu";
import { CSSProperties, useEffect } from "react";
import { useDispatch } from "react-redux";
import { setPath } from "../../stores/page/path";
export default function Metaverse() {
  const {
    unityProvider,
    isLoaded,
    loadingProgression,
    UNSAFE__detachAndUnloadImmediate: detachAndUnloadImmediate,
  } = useUnityContext({
    loaderUrl: "Build/WebGLFile.loader.js",
    dataUrl: "Build/WebGLFile.data",
    frameworkUrl: "Build/WebGLFile.framework.js",
    codeUrl: "Build/WebGLFile.wasm",
  });
  const dispatch = useDispatch();

  //닉네임 유무 확인
  //비로그인
  /////직접 닉네임 입력

  //로그인
  /////닉네임 안등록됨
  ////////닉네임 등록진행
  useEffect(() => {
    dispatch(setPath("metaverse"));
    return () => {
      setPath("");
    };
  }, []);

  useEffect(() => {
    return () => {
      detachAndUnloadImmediate().catch((reason) => {
        console.warn(reason);
      });
    };
  }, [detachAndUnloadImmediate]);

  const loadingPercentage = Math.round(loadingProgression * 100);
  const style: CSSProperties = {
    width: loadingPercentage + "%",
  };
  return (
    <>
      {isLoaded === false && (
        <div className="flex h-screen w-full items-center justify-center bg-blue-500">
          <div className="flex flex-col ">
            <span className="mb-8 block text-7xl font-bold">Loading...</span>
            <div className="h-10 w-full rounded-full bg-gray-200 dark:bg-gray-700">
              <div
                className="h-10 rounded-full bg-blue-600"
                style={style}
              ></div>
            </div>
          </div>
        </div>
      )}
      <Unity
        unityProvider={unityProvider}
        style={{ marginLeft: 50, width: 1400, height: 750 }}
      />
      <FloatingMenu />
    </>
  );
}
